package jp.co.metateam.library.controller;

import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.BookMstService;
import lombok.extern.log4j.Log4j2;

/**
 * 書籍関連クラス
 */
import java.util.Objects;

@Log4j2
@Controller

public class BookController {

    private final BookMstService bookMstService;

    @Autowired
    public BookController(BookMstService bookMstService) {
        this.bookMstService = bookMstService;
    }

    @GetMapping("/book/index")
    public String index(Model model) {
        // 書籍を全件取得
        List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();
        // findAvailableWithStockCountが書籍情報を全件取得コントローラー(ブックマストテーブルからとってくる)
        // Listは可変長の型（なんでもいれることができる型）（DBにいくつ入っているかわからないときは、なんでも入れられるListを使う）
        // 今回はBookMstDto型が入るList型を作っている
        // List<BookMstDto>型の変数名 bookMstList
        model.addAttribute("bookMstList", bookMstList);
        // htmlに対して表示したい値を詰めている
        // bookMstListの値をhtmlのattributeName:"bookMstList"に詰めているってこと
        return "book/index";
        // ここで画面に遷移させる
    }

    @GetMapping("/book/add")
    public String add(Model model) {
        if (!model.containsAttribute("bookMstDto")) {
            model.addAttribute("bookMstDto", new BookMstDto());
            // addAttribute（bookMstDto）に値を格納してreturnでjavaの値をhtmlに返す
            // addAttributeで値を取得してhtmlに表示させてくれるやつ
            // bookMstDtoにnew BookMstDtoを格納
            // modelが画面とコントローラを繋げるアノテーション
        }

        return "book/add";
    }

    @GetMapping("/book/edit/{id}")
    public String editBook(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        BookMstDto bookMst = bookMstService.findById(id); // ← サービスから取得
        model.addAttribute("bookMstDto", bookMst); // ← HTMLに渡す
        return "book/edit";
    }

    @PostMapping("/book/edit/{id}")
    public String update(@ModelAttribute BookMstDto bookMstDto, BindingResult result, RedirectAttributes ra,
            Model model) {
        // Model....リクエストの中だけで有効なデータ保持
        // RedirectAttributes...リダイレクト先までデータを渡すために使う

        // 編集処理
        boolean errIsbnFlg = false;
        // String isbnExist = bookMstDto.getTitle();
        boolean errTitleFlg = false;
        // String titleExist = bookMstDto.getTitle();

        // 書籍名とISBNの変更があるかのバリデーション
        BookMstDto original = bookMstService.findById(bookMstDto.getId());

        if (original == null) {
            ra.addFlashAttribute("errormessage", "該当する書籍が見つかりませんでした。");
            // model.addAttribute("errormessage", "該当する書籍が見つかりませんでした。"); // ← HTMLに渡す
            return "redirect:/book/index";
        }
        String originalTitle = original.getTitle();
        String newTitle = bookMstDto.getTitle();
        String originalIsbn = original.getIsbn();
        String newIsbn = bookMstDto.getIsbn();
        // newIsbnは、新しく入力されたISBN
        // originalIsbnは、もともとDBに登録されているISBN

        // タイトルが変わったかどうかを isTitleChanged に代入する
        boolean isTitleChanged = !Objects.equals(originalTitle, newTitle);
        boolean isIsbnChanged = !Objects.equals(originalIsbn, newIsbn);

        // 変更なしの場合⇣
        if (!isTitleChanged && !isIsbnChanged) {
            model.addAttribute("errorMessage", "変更がありませんでした。");
            model.addAttribute("bookMstDto", bookMstDto);
            return "book/edit";
        }

        // 書籍名の変更があった際のバリデーションチェック
        if (isTitleChanged) {
            errTitleFlg = isValidTitle(result, errTitleFlg, newTitle);
        }
        // ISBN変更ありのバリデーションチェック
        if (isIsbnChanged) {
            errIsbnFlg = isValidIsbn(result, errIsbnFlg, newIsbn);
            // ISBNが変更されていれば、isValidIsbn() メソッドを実行してバリデーションチェックを行い、その結果を errIsbnFlg に代入する
            errIsbnFlg = isValidWcheck(bookMstDto, result, errIsbnFlg);
        }

        // 「タイトルにエラーがある」または「ISBNにエラーがある」なら、{}内を実行する
        if (errTitleFlg || errIsbnFlg) {
            return "book/edit"; // 編集画面に戻す
        }

        try {
            // 正常な更新処理
            bookMstService.update(bookMstDto);
            model.addAttribute("errorMessage", "書籍情報を更新しました。");
            model.addAttribute("bookMstDto", bookMstDto);
            return "redirect:/book/index"; // 成功した場合は書籍一覧にリダイレクト

        } catch (Exception e) {
            log.error("Error during book edit: " + e.getMessage());
            ra.addFlashAttribute("bookMstDto", bookMstDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.bookMstDto", result);
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/book/edit/" + bookMstDto.getId(); // リダイレクト
        }

    }

    @PostMapping("/book/add")
    // addの画面で保存ボタンをオスとここに値が飛んでくる

    public String registBook(@Valid @ModelAttribute BookMstDto bookMstDto, BindingResult result, RedirectAttributes ra,
            Model model) {
        // BookMstDtoはクラス名（StringとかINTとかのイメージ）で、bookMstDtoは変数名 みたいなイメージ
        // BindingResult resultは フォームのバリデーション結果（入力チェックの結果）を受け取るためのオブジェクト

        boolean errTitleFlg = false;// 初期値がfalseってこと
        boolean errIsbnFlg = false;
        String titleExist = bookMstDto.getTitle();
        String isbnExist = bookMstDto.getIsbn();
        // isbnExistに格納↑

        errTitleFlg = isValidTitle(result, errTitleFlg, titleExist);
        errIsbnFlg = isValidIsbn(result, errIsbnFlg, isbnExist);
        errIsbnFlg = isValidWcheck(bookMstDto, result, errIsbnFlg);

        if (errTitleFlg || errIsbnFlg) {
            return "book/add";
        }

        try {
            // エラーが発生しそうな処理を try ブロックに書いて、実際にエラーが起きたときの対応を catch ブロックに書く。
            bookMstService.save(bookMstDto);
            // ○○サービス.△△を呼び出して、bookMstDtoに保存
            return "redirect:/book/index";
            // 書籍一覧画面へリダイレクト
            // redirect:の後のURLに対して再度GET通信をしにいく。そして一覧画面に書籍情報を追加して表示させることができる

        } catch (Exception e) {
            log.error(e.getMessage());
            ra.addFlashAttribute("bookDto", bookMstDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.bookMst", result);

            return "book/index";// 書籍一覧画面に飛ぶ

        }

    }

    private boolean isValidWcheck(BookMstDto bookMstDto, BindingResult result, boolean errIsbnFlg) {
        BookMst tesDto = bookMstService.selectByIsbn(bookMstDto.getIsbn());
        if (tesDto != null) {
            result.rejectValue("isbn", "error.value", "登録済みのISBNです");
            errIsbnFlg = true;
        }
        return errIsbnFlg;
    }

    private boolean isValidIsbn(BindingResult result, boolean errIsbnFlg, String isbnExist) {
        if (isbnExist == null || isbnExist.trim().isEmpty()) {
            result.rejectValue("isbn", "error.value", "ISBNは必須です");
            errIsbnFlg = true;
        } else {
            if (isbnExist.length() != 13) {
                // if文も試してここも試してね
                result.rejectValue("isbn", "error.value", "ISBNは13桁で入力してください");
                errIsbnFlg = true;
            }
            if (!String.valueOf(isbnExist).matches("^[0-9]+$")) {
                // 何通りか試したいときは、else{if{if}}にすると、3つめのパターンも通ってくれる
                result.rejectValue("isbn", "error.value", "ISBNは半角数字のみで入力してください");
                errIsbnFlg = true;
            }
        }
        return errIsbnFlg;
    }

    private boolean isValidTitle(BindingResult result, boolean errTitleFlg, String titleExist) {
        if (Objects.isNull(titleExist) || titleExist.trim().isEmpty()) {
            result.rejectValue("title", "error.value", "書籍名は必須です");
            errTitleFlg = true;

        } else if (titleExist != null && titleExist.length() >= 50) {
            // if (titleExist.length()>= 50) {
            result.rejectValue("title", "error.value", "書籍名は50字以内で入力してください");
            // "title", "error.value", "書籍名は50字で入力してください"はadd.htmlに値が返る
            errTitleFlg = true;
        }
        return errTitleFlg;
    }
}
