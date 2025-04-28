package jp.co.metateam.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public BookController(BookMstService bookMstService){
        this.bookMstService = bookMstService;
    }

    @GetMapping("/book/index")
    public String index(Model model) {
        // 書籍を全件取得
        List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();
        // findAvailableWithStockCountが書籍情報を全件取得コントローラー
        // Listは可変長の型（なんでもいれることができる型）（DBにいくつ入っているかわからないときは、なんでも入れられるListを使う）
        // 今回はBookMstDto型が入るList型を作っている
        //  List<BookMstDto>型の変数名 bookMstList
        model.addAttribute("bookMstList", bookMstList);
        // htmlに対して表示したい値を詰めている
        // bookMstListの値をhtmlのattributeName:"bookMstList"に詰めているってこと
        
        return "book/index";
        // ここで画面に表示させる
    }

    @GetMapping("/book/add")
    public String add(Model model) {
        if (!model.containsAttribute("bookMstDto")) {
            model.addAttribute("bookMstDto", new BookMstDto());
                 //addAttribute（bookMstDto）に値を格納してreturnでjavaの値をhtmlに返す
                //  addAttributeで値を取得してhtmlに表示させてくれるやつ
                //  bookMstDtoにnew BookMstDtoを格納
                // modelが画面とコントローラを繋げるアノテーション
        }

        return "book/add";
    }


    @PostMapping("/book/add")
    // addの画面で保存ボタンをオスとここに値が飛んでくる
    
   public String registBook(@Valid @ModelAttribute BookMstDto bookMstDto, BindingResult result,RedirectAttributes ra) {
                                         //BookMstDtoはクラス名（StringとかINTとかのイメージ）で、bookMstDtoは変数名　みたいなイメージ
            
                try{

                boolean errTitleFlg = false;//初期値がfalseってこと
                boolean errIsbnFlg = false;
                String title = bookMstDto.getTitle();
                                            //コントローラークラスのaccountDtoの中のイーメールをサービスクラスのselectByEmailに渡す
                String isbn = bookMstDto.getIsbn();
                           //employeeExistに格納↑
                if (Objects.isNull(title) || title.trim().isEmpty()) {
                        //    if(title == null || title.trim().isEmpty()){
                    result.rejectValue("title", "error.value", "書籍名は必須です");
                    errTitleFlg = true;
                            
                        }

                else if (title.length() > 50){
                    result.rejectValue("title", "error.value", "書籍名は50字で入力してください");
                                  //   "title", "error.value", "書籍名は50字で入力してください"はadd.htmlに値が返る
                    errTitleFlg = true;
                }
               
                if(isbn == null || isbn.trim().isEmpty()){
                        result.rejectValue("isbn", "error.value", "ISBNは必須です");
                        errIsbnFlg = true;  
                        
                    }
               
                else if (isbn.length() != 13){
                     result.rejectValue("isbn", "error.value", "ISBNは13桁で入力してください");
                     errIsbnFlg = true;
                    }
                else if (!String.valueOf(isbn).matches("\\d+")) {
                    result.rejectValue("isbn", "error.value", "ISBNは半角数字のみで入力してください");
                    errIsbnFlg = true; 
                    }
               
             else if (bookMstService.selectByIsbn(isbn) != null) {

                result.rejectValue("isbn", "error.value", "登録済みのISBNです");
                errIsbnFlg = true;

            }
                
            if (errTitleFlg || errIsbnFlg){
                return "book/add";
                // returnでこの処理終わり！みたいな意味
            }
     
                
    

                bookMstService.save(bookMstDto);
                  //○○サービス.△△を呼び出して、bookMstDtoに保存
                return "redirect:/book/index";
                // 書籍一覧画面へリダイレクト
                // redirect:の後のURLに対して再度GET通信をしにいく。そして一覧画面に書籍情報を追加して表示させることができる
            
            } catch (Exception e) {
                log.error(e.getMessage());
                ra.addFlashAttribute("bookDto", bookMstDto);
                ra.addFlashAttribute("org.springframework.validation.BindingResult.bookMst", result);
              

            return "book/add";//書籍一覧画面に飛ぶ



        }

        
    
    }
}