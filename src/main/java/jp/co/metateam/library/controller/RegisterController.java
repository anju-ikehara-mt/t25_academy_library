package jp.co.metateam.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.values.AuthorizationTypes;
import lombok.extern.slf4j.Slf4j;

/**
 * アカウント登録関連クラス
 */
@Slf4j
@Controller
public class RegisterController {

    private final AccountService accountService;

    @Autowired
    public RegisterController(AccountService accountService){
        this.accountService = accountService;
    }

    @GetMapping("/register")//URLに乗る
    public String registerForm(Model model) {
        model.addAttribute("authorizationTypes", AuthorizationTypes.values());

        if (!model.containsAttribute("accountDto")) {
            model.addAttribute("accountDto", new AccountDto());
        }

        return "register";//アカウント画面がreturnで帰ってくる
    }

    

    @PostMapping("/register")//URLに乗らない（POST通信だから）
    public String register(@Valid @ModelAttribute AccountDto accountDto, BindingResult result, RedirectAttributes ra) {
        try {

            boolean errEmailFlg = false;//初期値がfalseってこと
            boolean errEmpIdFlg = false;
            Account emailExist = this.accountService.selectByEmail(accountDto.getEmail());
                                        //コントローラークラスのaccountDtoの中のイーメールをサービスクラスのselectByEmailに渡す
            Account employeeExist = this.accountService.selectByEmployeeId(accountDto.getEmployeeId());
                       //employeeExistに格納↑
            if(emailExist != null){
                result.rejectValue("email", "error.value", "登録済みのメールアドレスです");
                errEmailFlg = true;//とってきたイーメールが空じゃないときはエラーです
            }
            if(employeeExist != null){
                result.rejectValue("employeeId", "error.value", "登録済みの社員番号です");
                errEmpIdFlg = true;//IDが空じゃないときはエラーです
            }
            if (errEmailFlg || errEmpIdFlg) {//どっちかがtrueだったら処理できないとしたい（エラー扱いにしたい）
                throw new Exception("Account already exists.");
            }

            accountService.save(accountDto);//アカウント登録処理。アカウントサービスのsaveメソッドにaccountDtoを入れる

            return "redirect:login"; //ログイン後の画面に飛ぶ
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("accountDto", accountDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.accountDto", result);

            return "redirect:register";
        }
    }
}