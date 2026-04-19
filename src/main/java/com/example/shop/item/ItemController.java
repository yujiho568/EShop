package com.example.shop.item;//com 폴더 안에 example 폴더 안에 shop 폴더
//에디터가 자동으로 폴더 압축해줌
//다른 파일에도 이 클래스를 사용하고 싶으면 package 필수

import com.example.shop.Comment;
import com.example.shop.CommentRepository;
import com.example.shop.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller//붙이면 api 다 사용 가능
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    private final ItemRepository itemRepository;

    private final S3Service s3Service;
    private final CommentRepository commentRepository;

    //public 없으면 같은 패키지 안에서만 사용 가능
    @GetMapping("/list")
    String list(){
        return "redirect:/list/page/1";
    }
    //탬플릿엔진 쓰면 서버/DB의 데이터를 html에 넣을 수 있음
    //Thymeleaf

    @GetMapping("/write")
    String write(){
        return "write";
    }

    @PostMapping("/add")
    String addPost(@ModelAttribute Item item){
        itemService.saveItem(item);
        return "redirect:/list";
    }

    @PostMapping("/edit/{id}")
    String editPost(@ModelAttribute Item item){
        itemService.updateItem(item);
        return "redirect:/list";
    }

    @GetMapping("/detail/{id}")
    String detail(@PathVariable Integer id, Model model) {
        try{
            Optional<Item> res = itemService.getItemById(id);
            if(res.isPresent()) {
                model.addAttribute("id", res.get().getId());
                model.addAttribute("title", res.get().getTitle());
                model.addAttribute("price", res.get().getPrice());
                model.addAttribute("image", res.get().getImage());

                List<Comment> comments = commentRepository.findByParentId(res.get().getId().longValue());
                model.addAttribute("comments", comments);
                return "detail";
            }
            else return "redirect:/list";
        }catch (Exception e){
            System.out.println(e.getMessage());
            return "redirect:/list";
        }
    }

    @GetMapping("/edit/{id}")
    String edit(@PathVariable Integer id, Model model){
        try{
            Optional<Item> res = itemService.getItemById(id);
            if(res.isPresent()) {
                model.addAttribute("id", res.get().getId());
                model.addAttribute("title", res.get().getTitle());
                model.addAttribute("price", res.get().getPrice());
                return "edit";
            }
            else return "redirect:/list";
        }catch (Exception e){
            System.out.println(e.getMessage());
            return "redirect:/list";
        }
    }

    @DeleteMapping("/item")
    ResponseEntity<String> deleteItem(@RequestParam Integer id){
        itemRepository.deleteById(id);
        return ResponseEntity.status(200).body("delete complete");
    }

    @GetMapping("/list/page/{id}")
    String getListPage(@PathVariable Integer id, Model model){
        if (id < 1) {
            return "redirect:/list/page/1";
        }
        Page<Item> result = itemRepository.findPageBy(PageRequest.of(id - 1, 5));
        model.addAttribute("items", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("currentPage", id);
        return "list";
    }

    @GetMapping("/presigned-url")
    @ResponseBody
    String getURL(@RequestParam String filename){
        var result = s3Service.createPresignedUrl("test/" + filename);
        return result;
    }

    @GetMapping("/search")
    String search(@RequestParam String searchText, @RequestParam(defaultValue="1") int page, Model model){
        int safePage = Math.max(page, 1);
        Page<Item> result = itemRepository.fullTextSearch(searchText, PageRequest.of(safePage - 1, 5));
        model.addAttribute("items", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("searchText", searchText);
        return "list";
    }

}
