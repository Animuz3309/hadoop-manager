package edu.scut.cs.hm.model;

import edu.scut.cs.hm.docker.model.image.SearchResult;

public interface SupportSearch {
    SearchResult search(String searchTerm, int page, int count);
}
