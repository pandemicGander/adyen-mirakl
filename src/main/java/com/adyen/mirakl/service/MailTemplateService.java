package com.adyen.mirakl.service;

import com.adyen.mirakl.config.Constants;
import com.adyen.mirakl.config.MiraklOperatorConfiguration;
import com.adyen.model.marketpay.Message;
import com.adyen.model.marketpay.ShareholderContact;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import io.github.jhipster.config.JHipsterProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.util.List;
import java.util.Locale;
import javax.annotation.Resource;

@Service
public class MailTemplateService {

    private static final String MIRAKL_SHOP = "miraklShop";
    private static final String MIRAKL_CALL_BACK_SHOP_URL = "miraklCallBackShopUrl";
    private static final String BASE_URL = "baseUrl";
    private static final String ERRORS = "errors";
    private static final String SHAREHOLDER = "shareholder";
    private static final String PAYOUT_ERROR = "payoutError";


    @Value("${miraklOperator.miraklEnvUrl}")
    private String miraklEnvUrl;


    private final JHipsterProperties jHipsterProperties;
    private final MailService mailService;
    private final SpringTemplateEngine templateEngine;
    private final MessageSource messageSource;

    @Resource
    private MiraklOperatorConfiguration miraklOperatorConfiguration;

    public MailTemplateService(final JHipsterProperties jHipsterProperties,
                               MailService mailService,
                               SpringTemplateEngine templateEngine,
                               MessageSource messageSource,
                               MiraklOperatorConfiguration miraklOperatorConfiguration) {
        this.jHipsterProperties = jHipsterProperties;
        this.mailService = mailService;
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
        this.miraklOperatorConfiguration = miraklOperatorConfiguration;
    }

    @Async
    public void sendMiraklShopEmailFromTemplate(MiraklShop miraklShop, Locale locale, String templateName, String titleKey) {
        Context context = new Context(locale);
        context.setVariable(MIRAKL_SHOP, miraklShop);
        context.setVariable(MIRAKL_CALL_BACK_SHOP_URL, getMiraklShopUrl(miraklShop.getId()));
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        mailService.sendEmail(miraklShop.getContactInformation().getEmail(), subject, content, false, true);
    }

    @Async
    public void sendShareholderEmailFromTemplate(final ShareholderContact shareholder, String shopId, Locale locale, String templateName, String titleKey) {
        Context context = new Context(locale);
        context.setVariable(SHAREHOLDER, shareholder);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        context.setVariable(MIRAKL_CALL_BACK_SHOP_URL, getMiraklShopUrl(shopId));
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        mailService.sendEmail(shareholder.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendSellerEmailWithErrors(MiraklShop miraklShop, List<String> errors) {
        Context context = new Context(Locale.getDefault());
        context.setVariable(MIRAKL_SHOP, miraklShop);
        context.setVariable(MIRAKL_CALL_BACK_SHOP_URL, getMiraklShopUrl(miraklShop.getId()));
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        context.setVariable(ERRORS, errors);
        String content = templateEngine.process("shopNotifications/sellerEmailWithErrors", context);
        String subject = messageSource.getMessage(Constants.Messages.EMAIL_ACCOUNT_HOLDER_VALIDATION_TITLE, null, Locale.getDefault());
        mailService.sendEmail(miraklShop.getContactInformation().getEmail(), subject, content, false, true);
    }

    @Async
    public void sendOperatorEmailWithErrors(MiraklShop miraklShop, List<String> errors) {
        Context context = new Context(Locale.getDefault());
        context.setVariable(MIRAKL_SHOP, miraklShop);
        context.setVariable(MIRAKL_CALL_BACK_SHOP_URL, getMiraklShopUrl(miraklShop.getId()));
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        context.setVariable(ERRORS, errors);
        String content = templateEngine.process("shopNotifications/operatorEmailWithErrors", context);
        String subject = messageSource.getMessage(Constants.Messages.EMAIL_ACCOUNT_HOLDER_VALIDATION_TITLE, null, Locale.getDefault());
        mailService.sendEmail(miraklOperatorConfiguration.getMiraklOperatorEmail(), subject, content, false, true);
    }

    @Async
    public void sendOperatorEmailPayoutFailure(MiraklShop miraklShop, Message message) {
        Context context = new Context(Locale.getDefault());
        context.setVariable(MIRAKL_SHOP, miraklShop);
        context.setVariable(MIRAKL_CALL_BACK_SHOP_URL, getMiraklShopUrl(miraklShop.getId()));
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        context.setVariable(PAYOUT_ERROR, "(" + message.getCode() + ") " + message.getText());
        String content = templateEngine.process("shopNotifications/operatorEmailPayoutFailed", context);
        String subject = messageSource.getMessage(Constants.Messages.EMAIL_ACCOUNT_HOLDER_PAYOUT_FAILED_TITLE, null, Locale.getDefault());
        mailService.sendEmail(miraklOperatorConfiguration.getMiraklOperatorEmail(), subject, content, false, true);
    }

    private String getMiraklShopUrl(String miraklShopId) {
        return String.format("%s/mmp/shop/account/shop/%s", miraklEnvUrl, miraklShopId);
    }

    public String getMiraklEnvUrl() {
        return miraklEnvUrl;
    }

    public void setMiraklEnvUrl(final String miraklEnvUrl) {
        this.miraklEnvUrl = miraklEnvUrl;
    }

}
