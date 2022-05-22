package com.example.moneytransfer.controller;

import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.paging.Paged;
import com.example.moneytransfer.request.RefreshTransactionRequest;
import com.example.moneytransfer.request.SendTransactionRequest;
import com.example.moneytransfer.request.UpdateStatusRequest;
import com.example.moneytransfer.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.keycloak.KeycloakSecurityContext;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final HttpServletRequest request;
    private final TransactionService transactionService;

    @RolesAllowed("USER_ROLE")
    @GetMapping("/")
    public String sendPage(@RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                           @RequestParam(value = "size", required = false, defaultValue = "6") int size,
                           @RequestParam(value = "sortField", defaultValue = "dateCreated") String sortField,
                           @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
                           Model model) {
        model.addAttribute("sendTrReq", new SendTransactionRequest());
        model.addAttribute("refreshReq", new RefreshTransactionRequest());
        return sent(pageNumber, size, sortField, sortDir,model);
    }

    @PostMapping("/sendForm")
    public String sendForm(@Valid @ModelAttribute SendTransactionRequest sendTrReq) {
        transactionService.send(sendTrReq);
        return "redirect:/";
    }

    @PostMapping("/refresh")
    public String refreshTransaction(@Valid @ModelAttribute RefreshTransactionRequest refreshReq) {
        transactionService.refresh(refreshReq);
        return "redirect:/";
    }

    @GetMapping("/received")
    public String received(@RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                           @RequestParam(value = "size", required = false, defaultValue = "6") int size,
                           @RequestParam(value = "sortField", defaultValue = "dateCreated") String sortField,
                           @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
                           Model model) {
        configCommonAttributes(model);
        Paged<Transaction> paged = transactionService.getAllByReceiver(getKeycloakSecurityContext().getToken().getPreferredUsername(), pageNumber, size, sortField, sortDir);
        model.addAttribute("transactions", paged);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("totalItems", paged.getPage().getTotalElements());
        return "transactions-received";
    }

    @GetMapping("/sent")
    public String sent(@RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                       @RequestParam(value = "size", required = false, defaultValue = "6") int size,
                       @RequestParam(value = "sortField", defaultValue = "dateCreated") String sortField,
                       @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
                       Model model) {
        configCommonAttributes(model);
        Paged<Transaction> paged = transactionService.getAllBySender(getKeycloakSecurityContext().getToken().getPreferredUsername(), pageNumber, size, sortField, sortDir);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("transactions", paged);
        model.addAttribute("totalItems", paged.getPage().getTotalElements());
        return "transactions-sent";
    }

    @PostMapping("/checkcode")
    public String checkode(@RequestParam(value = "code") String code) {
        transactionService.receive(code);
        return "redirect:/received/";
    }

    @RolesAllowed("ADMIN_ROLE")
    @GetMapping("/admin-console")
    public String getAllTransactions(@RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                                     @RequestParam(value = "size", required = false, defaultValue = "6") int size,
                                     @RequestParam(value = "sortField", defaultValue = "dateCreated") String sortField,
                                     @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
                                     Model model) {
        configCommonAttributes(model);
        Paged<Transaction> paged = transactionService.getAll(pageNumber, size, sortField, sortDir);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("totalItems", paged.getPage().getTotalElements());
        model.addAttribute("request", new UpdateStatusRequest());
        model.addAttribute("transactions", paged);
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
