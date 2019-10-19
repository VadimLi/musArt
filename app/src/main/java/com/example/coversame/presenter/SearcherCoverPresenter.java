package com.example.coversame.presenter;

import com.example.coversame.model.Album;

import java.util.List;

public class SearcherCoverPresenter {

    private SearcherCoverView searcherCoverView;

    public void attachSearcherCoverView(SearcherCoverView searcherCoverView) {
        this.searcherCoverView = searcherCoverView;
    }

    public void detachSearcherVoverView() {
        searcherCoverView = null;
    }

    public void searchCovers() {

    }
    
    public interface SearcherCoverView {
        void displayCovers(List<Album> albumList);
    }

}
