package org.choongang.board.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.board.services.WishListService;
import org.choongang.global.rests.JSONData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wish")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService service;

    @GetMapping("/list")
    public JSONData list() {
        List<Long> seqs = service.getList();

        return new JSONData(seqs);
    }

    // 추가
    @GetMapping("/{seq}")
    public ResponseEntity<Void> add(@PathVariable("seq") Long seq) {

        service.add(seq);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 추가
    @DeleteMapping("/{seq}")
    public ResponseEntity<Void> remove(@PathVariable("seq") Long seq) {
        service.remove(seq);

        return ResponseEntity.ok().build();
    }
}
