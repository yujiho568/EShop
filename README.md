# EShop — MSA 이커머스 주문 시스템 실습

Spring Boot + Kafka 기반의 MSA 학습 프로젝트입니다.  
멱등성, 재시도, 정합성, Saga 패턴, 보상 트랜잭션, 관찰성을 직접 구현하며 실습합니다.

---

## 목차

1. [아키텍처 개요](#아키텍처-개요)
2. [Saga 흐름 설계](#saga-흐름-설계)
3. [Kafka 토픽 구조](#kafka-토픽-구조)
4. [모듈별 코드 설명](#모듈별-코드-설명)
   - [common](#common)
   - [order-service](#order-service)
   - [inventory-service](#inventory-service-진행중)
   - [payment-service](#payment-service-예정)
   - [notification-service](#notification-service-예정)
5. [인프라 구성](#인프라-구성)
6. [실행 방법](#실행-방법)
7. [학습 포인트 정리](#학습-포인트-정리)

---

## 아키텍처 개요

```
Client
  │
  ▼
order-service (8081)   ── publishes ──▶  Kafka
  │                                        │
  │  consumes ◀──────────────────────────  │
  │                                        │
  │                           ┌────────────┼────────────┐
  │                           ▼            ▼            ▼
  │                   inventory-service  payment-service  notification-service
  │                      (8083)            (8082)           (8084)
  │
  ▼
MySQL / H2 (각 서비스 독립 DB)
```

**설계 원칙**
- 각 서비스는 **독립된 데이터베이스**를 가짐 (DB per Service)
- 서비스 간 직접 HTTP 호출 없음 — **Kafka 이벤트로만 통신**
- Saga 패턴: **Choreography 방식** (중앙 오케스트레이터 없이 이벤트로 흐름 제어)

---

## Saga 흐름 설계

### Happy Path (정상 주문)

```
1. POST /api/orders  →  order-service
2. order-service      →  DB: Order(INVENTORY_CHECKING) 저장
                      →  Kafka: order.created 발행

3. inventory-service  ←  Kafka: order.created 수신
                      →  재고 예약 (낙관적 락으로 중복 차감 방지)
                      →  Kafka: inventory.reserved 발행

4. order-service      ←  Kafka: inventory.reserved 수신
                      →  DB: Order(PAYMENT_PROCESSING) 업데이트

5. payment-service    ←  Kafka: inventory.reserved 수신
                      →  멱등성 키 체크 (중복 결제 방지)
                      →  결제 처리
                      →  Kafka: payment.completed 발행

6. order-service      ←  Kafka: payment.completed 수신
                      →  DB: Order(COMPLETED) 업데이트
                      →  Kafka: order.completed 발행

7. notification-service ← Kafka: order.completed 수신
                        →  알림 발송
```

### 재고 부족 시 (보상 트랜잭션 필요 없음)

```
3. inventory-service  ←  Kafka: order.created 수신
                      →  재고 부족 확인
                      →  Kafka: inventory.failed 발행

4. order-service      ←  Kafka: inventory.failed 수신
                      →  DB: Order(CANCELLED) 업데이트
                      →  Kafka: order.cancelled 발행

5. notification-service ← Kafka: order.cancelled 수신 → 취소 알림
```

### 결제 실패 시 (보상 트랜잭션 발동)

```
5. payment-service    ←  Kafka: inventory.reserved 수신
                      →  결제 실패
                      →  Kafka: payment.failed 발행

6. order-service      ←  Kafka: payment.failed 수신
                      →  DB: Order(CANCELLED) 업데이트
                      →  Kafka: order.cancelled 발행  ◀── 보상 트리거

7. inventory-service  ←  Kafka: order.cancelled 수신
                      →  예약된 재고 롤백 (보상 트랜잭션)  ◀── 핵심!

8. notification-service ← Kafka: order.cancelled 수신 → 취소 알림
```

---

## Kafka 토픽 구조

| 토픽 | 발행자 | 소비자 | 설명 |
|------|--------|--------|------|
| `order.created` | order-service | inventory-service | Saga 시작 |
| `inventory.reserved` | inventory-service | order-service, payment-service | 재고 예약 성공 |
| `inventory.failed` | inventory-service | order-service | 재고 부족 |
| `payment.completed` | payment-service | order-service | 결제 성공 |
| `payment.failed` | payment-service | order-service | 결제 실패 |
| `order.completed` | order-service | notification-service | 주문 완료 |
| `order.cancelled` | order-service | inventory-service, notification-service | 주문 취소 (보상 트리거) |

---

## 모듈별 코드 설명

### common

**위치**: `common/src/main/java/com/example/eshop/common/event/`

서비스 간 공유되는 Kafka 이벤트 DTO 모음입니다.

#### `Topics.java`

```java
public final class Topics {
    public static final String ORDER_CREATED      = "order.created";
    public static final String ORDER_COMPLETED    = "order.completed";
    public static final String ORDER_CANCELLED    = "order.cancelled";
    public static final String INVENTORY_RESERVED = "inventory.reserved";
    public static final String INVENTORY_FAILED   = "inventory.failed";
    public static final String PAYMENT_COMPLETED  = "payment.completed";
    public static final String PAYMENT_FAILED     = "payment.failed";
}
```

토픽 이름을 상수로 관리합니다. 서비스마다 문자열을 직접 쓰면 오타로 인한 버그가 생기기 쉽기 때문에 common 모듈에서 단일 진실 공급원(Single Source of Truth)으로 관리합니다.

#### 이벤트 클래스 설계

모든 이벤트는 Java `record`로 정의됩니다. 이벤트마다 공통으로 포함하는 필드:

| 필드 | 타입 | 목적 |
|------|------|------|
| `eventId` | `String (UUID)` | 이벤트 고유 ID — 중복 소비 감지에 사용 |
| `orderId` | `String` | Saga 전체를 관통하는 상관관계 ID |
| `occurredAt` | `Instant` | 이벤트 발생 시각 |

```java
// 예: OrderCreatedEvent
public record OrderCreatedEvent(
    String eventId,   // 이벤트 중복 감지용
    String orderId,   // Saga 상관관계 ID
    String memberId,
    Long productId,
    int quantity,
    BigDecimal totalAmount,
    Instant occurredAt
) {}
```

**왜 eventId가 필요한가?**  
Kafka는 at-least-once 전달을 보장합니다. 즉, 네트워크 장애나 컨슈머 재시작 시 같은 메시지가 두 번 이상 도착할 수 있습니다. 소비자 측에서 `eventId`를 처리 여부 테이블에 기록해두면 중복 소비를 방지할 수 있습니다(멱등 소비자 패턴).

---

### order-service

**위치**: `order-service/src/main/java/com/example/eshop/order/`  
**포트**: 8081

Saga의 진입점이자 흐름 조율 역할을 합니다. 주문을 생성하고, 다른 서비스들의 응답 이벤트에 따라 주문 상태를 전환합니다.

#### `Order.java` (도메인 엔티티)

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    private String orderId;           // UUID — DB auto-increment 아님
    private String memberId;
    private Long productId;
    private int quantity;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;       // 상태 머신
    private String failureReason;
}
```

**왜 PK를 UUID로?**  
여러 서비스가 독립적으로 ID를 생성해야 할 때, DB auto-increment는 DB 조회 없이는 ID를 미리 알 수 없습니다. UUID를 애플리케이션 레이어에서 생성하면 DB 없이도 ID를 먼저 결정하고 이벤트에 포함시킬 수 있습니다.

#### `OrderStatus.java` (상태 머신)

```
PENDING
  │
  ▼
INVENTORY_CHECKING  ← order.created 발행 직후
  │
  ├─[inventory.reserved]──▶ PAYMENT_PROCESSING
  │                              │
  │                    [payment.completed]──▶ COMPLETED
  │                              │
  │                    [payment.failed]──────▶ CANCELLED
  │
  └─[inventory.failed]──────────────────────▶ CANCELLED
```

상태 전환 메서드는 엔티티 내부에서만 호출 가능하도록 설계했습니다:

```java
public void markInventoryChecking() { this.status = OrderStatus.INVENTORY_CHECKING; }
public void markPaymentProcessing() { this.status = OrderStatus.PAYMENT_PROCESSING; }
public void complete()              { this.status = OrderStatus.COMPLETED; }
public void cancel(String reason)   { this.status = OrderStatus.CANCELLED; this.failureReason = reason; }
```

#### `OrderService.java` (Saga 조율 로직)

```java
// Saga 시작
@Transactional
public Order createOrder(CreateOrderRequest req) {
    Order order = Order.create(orderId, ...);
    order.markInventoryChecking();
    orderRepository.save(order);                    // 1) DB 저장
    publisher.publishOrderCreated(event);           // 2) 이벤트 발행
    return order;
}
```

> **주의**: DB 저장과 Kafka 발행이 하나의 메서드에 있지만 다른 트랜잭션입니다.  
> DB 커밋 후 Kafka 발행 전에 장애가 나면 이벤트가 유실될 수 있습니다.  
> 이를 완전히 해결하려면 **Transactional Outbox 패턴**이 필요합니다 (추후 실습 항목).

**결제 실패 시 보상 트랜잭션 트리거**:

```java
@Transactional
public void onPaymentFailed(PaymentFailedEvent event) {
    order.cancel(event.reason());                   // 주문 취소
    OrderCancelledEvent cancelled = OrderCancelledEvent.of(...);
    publisher.publishOrderCancelled(cancelled);     // order.cancelled 발행
    // → inventory-service가 이걸 소비해서 재고를 복원 (보상 트랜잭션)
}
```

#### `OrderEventConsumer.java` (Kafka 소비자)

```java
@KafkaListener(topics = Topics.INVENTORY_RESERVED, groupId = "order-service")
public void onInventoryReserved(InventoryReservedEvent event) { ... }

@KafkaListener(topics = Topics.PAYMENT_FAILED, groupId = "order-service")
public void onPaymentFailed(PaymentFailedEvent event) { ... }
```

**`groupId = "order-service"`의 의미**:  
같은 groupId를 공유하는 컨슈머들은 파티션을 나눠 소비합니다. order-service 인스턴스가 3개 뜨면 Kafka가 파티션을 분배해 중복 소비를 막습니다. 반면 notification-service도 같은 토픽을 소비하지만 groupId가 다르기 때문에 독립적으로 소비합니다.

#### `KafkaConfig.java` (Kafka 설정)

```java
// 프로듀서: 타입 정보를 헤더에 포함시켜 역직렬화 시 타입을 알 수 있게 함
props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);

// 컨슈머: 신뢰할 패키지 명시 (보안)
deserializer.addTrustedPackages("com.example.eshop.*");
```

---

### inventory-service (진행중)

**위치**: `inventory-service/`  
**포트**: 8083

구현 예정 핵심 포인트:

- **낙관적 락 (`@Version`)**: 동시에 여러 주문이 같은 재고를 차감하려 할 때 하나만 성공하도록
- **멱등 소비**: `orderId`로 이미 처리한 예약인지 확인 후 중복 차감 방지
- **보상 트랜잭션**: `order.cancelled` 수신 시 예약된 재고 복원

```java
// 예정 코드 - 낙관적 락
@Entity
public class Stock {
    private Long productId;
    private int quantity;

    @Version               // ← 낙관적 락 핵심
    private Long version;  // 동시 수정 시 OptimisticLockingFailureException 발생
}
```

---

### payment-service (예정)

**포트**: 8082

구현 예정 핵심 포인트:

- **멱등성 키 테이블**: 같은 `orderId`로 두 번 결제 요청이 와도 한 번만 처리
- **중복 결제 방지**: `idempotency_keys` 테이블에 처리 결과를 캐싱

```java
// 예정 코드 - 멱등성 체크
@Transactional
public PaymentResult processPayment(String orderId, BigDecimal amount) {
    // 이미 처리된 요청인지 확인
    return idempotencyKeyRepository.findByOrderId(orderId)
        .map(key -> key.getCachedResult())          // 캐시된 결과 반환
        .orElseGet(() -> doProcessAndSave(orderId, amount)); // 실제 처리
}
```

---

### notification-service (예정)

**포트**: 8084

`order.completed`, `order.cancelled` 수신 후 알림 발송 (로그 기반 시뮬레이션).

---

## 인프라 구성

### docker-compose.yml

```yaml
services:
  kafka:        # 이벤트 브로커 (port 9092)
  kafka-ui:     # Kafka 토픽/메시지 시각화 (port 8090)
  mysql:        # 운영 DB - 서비스별 독립 스키마 (port 3306)
  zipkin:       # 분산 추적 UI (port 9411)
```

**서비스별 DB 스키마 분리** (`docker/mysql/init.sql`):

```sql
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE inventory_db;
CREATE DATABASE notification_db;
```

각 서비스가 다른 서비스의 DB에 직접 접근하지 않는 것이 MSA의 핵심 원칙입니다.

### settings.gradle (멀티모듈)

```groovy
include 'common'
include 'order-service'
include 'payment-service'
include 'inventory-service'
include 'notification-service'
```

`common` 모듈은 다른 서비스들이 `implementation project(':common')`으로 의존합니다.

---

## 실행 방법

### 1. 인프라 실행

```bash
docker-compose up -d
```

| 서비스 | URL |
|--------|-----|
| Kafka UI | http://localhost:8090 |
| Zipkin | http://localhost:9411 |
| MySQL | localhost:3306 |

### 2. 서비스 실행 (각 터미널에서)

```bash
./gradlew :order-service:bootRun
./gradlew :inventory-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :notification-service:bootRun
```

### 3. 주문 생성 테스트

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": "user-1",
    "productId": 100,
    "quantity": 2,
    "totalAmount": 50000
  }'
```

### 4. 주문 상태 조회

```bash
curl http://localhost:8081/api/orders/{orderId}
```

---

## 학습 포인트 정리

| 개념 | 구현 위치 | 설명 |
|------|-----------|------|
| **MSA** | 전체 구조 | 서비스별 독립 빌드/배포/DB |
| **Choreography Saga** | `OrderService` + 각 서비스 consumer | 중앙 조율자 없이 이벤트로 흐름 제어 |
| **보상 트랜잭션** | `OrderService.onPaymentFailed()` → inventory-service | 결제 실패 시 재고 복원 |
| **멱등성** | payment-service `idempotency_keys` | 같은 요청 두 번 와도 한 번만 처리 |
| **중복 소비 방지** | 각 consumer의 `eventId` 체크 | Kafka at-least-once 보완 |
| **재고 동시성** | inventory-service `@Version` | 낙관적 락으로 이중 차감 방지 |
| **분산 추적** | Micrometer Tracing + Zipkin | traceId로 서비스 간 요청 추적 |
| **구조화 로그** | `application.yml` logging pattern | traceId를 로그에 자동 포함 |
| **Outbox 패턴** | (TODO) | DB 저장과 Kafka 발행의 원자성 보장 |

### 장애 시나리오 재현 방법 (예정)

```bash
# 재고 부족 시나리오
# → inventory-service에서 재고를 0으로 세팅 후 주문

# 결제 실패 시나리오
# → payment-service에 실패 모드 활성화 엔드포인트 추가

# Kafka 장애 시나리오
# → docker-compose stop kafka 후 주문 → 재시작 후 이벤트 처리 확인
```

---

## 구현 진행 현황

- [x] 인프라 구성 (docker-compose, multi-module Gradle)
- [x] common 모듈 (이벤트 클래스, 토픽 상수)
- [x] order-service (주문 생성, Saga 흐름 조율, 상태 머신)
- [ ] inventory-service (재고 예약, 낙관적 락, 보상 트랜잭션)
- [ ] payment-service (멱등성, 중복 결제 방지)
- [ ] notification-service (이벤트 수신, 알림)
- [ ] 테스트 코드 (EmbeddedKafka 통합 테스트)
- [ ] 관찰성 (Zipkin 분산 추적, MDC 로그)
