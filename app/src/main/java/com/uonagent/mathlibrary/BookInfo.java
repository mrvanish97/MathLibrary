package com.uonagent.mathlibrary;

public class BookInfo {
    public BookInfo(int _id, String title, String author, String isbn, int quant) {
        this._id = _id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.quant = quant;
    }

    int _id;
    String title;
    String author;
    String isbn;
    Integer quant;
}