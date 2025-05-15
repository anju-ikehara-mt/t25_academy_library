package jp.co.metateam.library.model;

import java.security.Timestamp;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 書籍マスタDTO
 */
@Getter
@Setter
public class BookMstDto {
    
    private Long id; //is(変数)にはLong型しか入らないよ
    
    private String isbn;//add.htmlのISBNで使う変数名？と揃える

    private String title;//title（変数）にはStringしか入らないよーを定義

    private Timestamp deletedAt;

    private BookMst bookMst;//bookMst(変数)にはBookMstしか入らないよーを定義SS

    // private LocalDateTime datetime;
}
