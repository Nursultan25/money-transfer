package com.example.moneytransfer.controller;

import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.request.SendTransactionRequest;
import com.example.moneytransfer.request.UpdateStatusRequest;
import com.example.moneytransfer.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakSecurityContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final HttpServletRequest request;
    private final TransactionService transactionService;

    @RolesAllowed("USER_ROLE")
    @GetMapping("/")
    public String sendPage(Model model) {
        model.addAttribute("sendTrReq", new SendTransactionRequest());
        return sent(model);
    }

    @PostMapping("/sendForm")
    public String sendForm(@Valid @ModelAttribute SendTransactionRequest sendTrReq) {
        transactionService.send(sendTrReq);
        return "redirect:/";
    }

    @GetMapping("/received")
    public String received(Model model) {
        configCommonAttributes(model);

        model.addAttribute("transactions", transactionService.getAllByReceiver(getKeycloakSecurityContext().getToken().getPreferredUsername()));
        return "transactions-received";
    }

    @GetMapping("/sent")
    public String sent(Model model) {
        configCommonAttributes(model);
        model.addAttribute("transactions", transactionService.getAllBySender(getKeycloakSecurityContext().getToken().getPreferredUsername()));
        return "transactions-sent";
    }

    @PostMapping("/checkcode")
    public String checkode(@RequestParam(value = "code") String code) {
        transactionService.receive(code);
        return "redirect:/received/";
    }

    @RolesAllowed("ADMIN_ROLE")
    @GetMapping("/admin-console")
    public String getAllTransactions(Model model) {
        configCommonAttributes(model);
        model.addAttribute("request", new UpdateStatusRequest());
        model.addAttribute("transactions", transactionService.getAll());
        return "admin-console";
    }

    @RolesAllowed("ADMIN_ROLE")
    @PostMapping("/changestat")
    public String changeStatus(Model model, @ModelAttribute UpdateStatusRequest request) {
        transactionService.update(request.getId(), request.getNewStatus().replaceAll(",", ""));
        return "redirect:/admin-console";
    }

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request) throws ServletException {
        request.logout();
        return "redirect:/";
    }

    public void configCommonAttributes(Model model) {
        model.addAttribute("name", getKeycloakSecurityContext().getToken().getPreferredUsername());
    }

    private KeycloakSecurityContext getKeycloakSecurityContext() {
        return (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
    }
}
