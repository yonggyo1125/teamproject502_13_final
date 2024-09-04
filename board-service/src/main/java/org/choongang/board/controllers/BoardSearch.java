package org.choongang.board.controllers;

import lombok.Data;
import org.choongang.global.CommonSearch;

import java.util.List;

@Data
public class BoardSearch extends CommonSearch {
    private String bid;
    private List<String> bids;

    private String bName;
    private boolean active;
}
