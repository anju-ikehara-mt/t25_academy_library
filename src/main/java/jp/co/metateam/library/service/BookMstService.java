package jp.co.metateam.library.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.repository.BookMstRepository;

@Service
public class BookMstService {

    private final BookMstRepository bookMstRepository;

    @Autowired
    public BookMstService(BookMstRepository bookMstRepository) {
        this.bookMstRepository = bookMstRepository;
    }

    public BookMst selectByTitle(String title) {
        return this.bookMstRepository.findByTitle(title).orElse(null);
    }

    public BookMst selectByIsbn(String isbn) {
        return this.bookMstRepository.findByIsbn(isbn).orElse(null);
    }

    public BookMstDto findById(Long id) {
        BookMst book = bookMstRepository.findById(id).orElse(null);
        if (book == null)
            return null;
        BookMstDto dto = new BookMstDto(); // DTO を作成。
        // new BookMstDto()が、新しいインスタンス（オブジェクト）。下のId、Title、Isbnを入れる箱（オブジェクト）を作っている
        dto.setId(book.getId()); // 値をコピー
        dto.setTitle(book.getTitle());
        dto.setIsbn(book.getIsbn()); // DTOを返す
        return dto;
    }

    // updateメソッド
    @Transactional
    public void update(BookMstDto bookMstDto) {
        Optional<BookMst> optional = bookMstRepository.findById(bookMstDto.getId());
        // ID をもとに、データベースから該当する書籍データを探す
        // Optional は「存在するかもしれないし、しないかもしれない」ことを表す型
        if (optional.isPresent()) {
            // 書籍が見つかったかどうかをチェック（見つかっていたら true）
            BookMst book = optional.get();
            book.setTitle(bookMstDto.getTitle());
            book.setIsbn(bookMstDto.getIsbn());
            // データベースから取得した書籍エンティティに対して、DTOから渡されたタイトルとISBNをセット（＝上書き）
            bookMstRepository.save(book);
        } else {
            throw new IllegalArgumentException("対象の書籍が見つかりません。");
        }
    }

    public List<BookMstDto> findAvailableWithStockCount() {
        List<BookMst> books = this.bookMstRepository.findLimitedBook();
        List<BookMstDto> bookMstDtoList = new ArrayList<BookMstDto>();

        // 書籍の在庫数を取得
        // FIXME: 現状は書籍ID毎にDBに問い合わせている。一度のSQLで完了させたい。
        for (int i = 0; i < books.size(); i++) {
            BookMst book = books.get(i);
            BookMstDto bookMstDto = new BookMstDto();
            bookMstDto.setId(book.getId());
            bookMstDto.setIsbn(book.getIsbn());
            bookMstDto.setTitle(book.getTitle());
            bookMstDtoList.add(bookMstDto);
        }

        return bookMstDtoList;
    }

    @Transactional
    public void save(BookMstDto bookMstDto) {
        try {
            // BookMstDtoからBookMstへの変換
            BookMst BookMst = new BookMst();

            BookMst.setTitle(bookMstDto.getTitle());
            // setTitleに入力値を設定する
            BookMst.setIsbn(bookMstDto.getIsbn());

            // データベースへの保存
            this.bookMstRepository.save(BookMst);
            // bookMstDtoの値をDBのsaveに引数として渡す
        } catch (Exception e) {
            throw e;
        }
    }

}
