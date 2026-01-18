package com.example.shop.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public void saveItem(Item item){
        itemRepository.save(item);
    }
    public void updateItem(Item item){
        if(item.getId() != null) {
            if(item.getPrice() < 0)
                throw new IllegalArgumentException("Price must be natural number");
            else if(item.getTitle().length() > 100 || item.getTitle().isEmpty())
                throw new IllegalArgumentException("Title must be between 0 and 100");
            else itemRepository.save(item);
        } else {
            throw new IllegalArgumentException("수정할 item의 id가 없습니다.");
        }
    }
    public Optional<Item> getItemById(Integer id){
        return itemRepository.findById(id);
    }

}
