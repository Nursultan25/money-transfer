package com.example.moneytransfer.controller;

import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.paging.Paged;
import com.example.moneytransfer.request.RefreshTransactionRequest;
import com.example.moneytransfer.request.SendTransactionRequest;
import com.example.moneytransfer.request.UpdateStatusRequest;
import com.example.moneytransfer.service.TransactionService;
import com.example.moneytransfer.utils.DateConverter;
import lombok.RequiredArgsConstructor;
import org.keycloak.KeycloakSecurityContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final HttpServletRequest request;
    private final TransactionService transactionService;

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

    @RolesAllowed("USER_ROLE")
    @GetMapping("/")
    public String sent(@RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                       @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                       @RequestParam(value = "sortField", defaultValue = "dateCreated") String sortField,
                       @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
                       Model model) {
        configCommonAttributes(model);
        List<Transaction> allTransactions = transactionService.getStatistics(getKeycloakSecurityContext().getToken().getPreferredUsername(),
                DateConverter.convertToDateViaInstant(LocalDateTime.now().minusMonths(1)),
                DateConverter.convertToDateViaInstant(LocalDateTime.now()));
        Paged<Transaction> paged = transactionService.getAllBySender(getKeycloakSecurityContext().getToken().getPreferredUsername(),
                pageNumber, size, sortField, sortDir,
                DateConverter.convertToDateViaInstant(LocalDateTime.now().minusMonths(1)),
                DateConverter.convertToDateViaInstant(LocalDateTime.now()));
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("transactions", paged);
        model.addAttribute("totalItems", paged.getPage().getTotalElements());
        model.addAttribute("sendTrReq", new SendTransactionRequest());
        model.addAttribute("refreshReq", new RefreshTransactionRequest());
        model.addAttribute("totalAmount", transactionService.calcTotalAmount(allTransactions));
        return "transactions-sent";
    }

    @GetMapping("/pickDates")
    public String getStatistics(@RequestParam(value = "dateFrom") @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFrom,
                                @RequestParam(value = "dateTo") @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateTo,
                                @RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                                @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                @RequestParam(value = "sortField", defaultValue = "dateCreated") String sortField,
                                @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
                                Model model) {
        configCommonAttributes(model);
        Paged<Transaction> paged = transactionService.getAllBySender(getKeycloakSecurityContext().getToken().getPreferredUsername(),
                pageNumber, size, sortField, sortDir, dateFrom, dateTo);
        List<Transaction> allTransactions = transactionService.getStatistics(getKeycloakSecurityContext().getToken().getPreferredUsername(),
                dateFrom, dateTo);
        model.addAttribute("transactions", paged);
        model.addAttribute("totalAmount", transactionService.calcTotalAmount(allTransactions));
        model.addAttribute("sendTrReq", new SendTransactionRequest());
        model.addAttribute("refreshReq", new RefreshTransactionRequest());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
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
                                     @RequestParam(value = "size", required = false, defaultValue = "10") int size,
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
