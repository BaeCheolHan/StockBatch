package com.my.stock.job;

import com.my.stock.base.BaseBatch;
import com.my.stock.rdb.entity.*;
import com.my.stock.rdb.repository.AccountDailyReturnRepository;
import com.my.stock.rdb.repository.BankAccountRepository;
import com.my.stock.rdb.repository.ExchangeRateRepository;
import com.my.stock.rdb.repository.StocksRepository;
import com.my.stock.redis.entity.KrNowStockPrice;
import com.my.stock.redis.entity.OverSeaNowStockPrice;
import com.my.stock.redis.repository.KrNowStockPriceRepository;
import com.my.stock.redis.repository.OverSeaNowStockPriceRepository;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.configuration.annotation.StepScope;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Configuration
public class AccountTwrDailyJobConfiguration extends BaseBatch {

    private final EntityManagerFactory entityManagerFactory;
    private final BankAccountRepository bankAccountRepository;
    private final StocksRepository stocksRepository;
    private final KrNowStockPriceRepository krNowStockPriceRepository;
    private final OverSeaNowStockPriceRepository overSeaNowStockPriceRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final AccountDailyReturnRepository accountDailyReturnRepository;

    public AccountTwrDailyJobConfiguration(
            EntityManagerFactory entityManagerFactory,
            BankAccountRepository bankAccountRepository,
            StocksRepository stocksRepository,
            KrNowStockPriceRepository krNowStockPriceRepository,
            OverSeaNowStockPriceRepository overSeaNowStockPriceRepository,
            ExchangeRateRepository exchangeRateRepository,
            AccountDailyReturnRepository accountDailyReturnRepository
    ) {
        super("AccountTwrDailyJob", "0 5/30 * * * ?", null);
        this.entityManagerFactory = entityManagerFactory;
        this.bankAccountRepository = bankAccountRepository;
        this.stocksRepository = stocksRepository;
        this.krNowStockPriceRepository = krNowStockPriceRepository;
        this.overSeaNowStockPriceRepository = overSeaNowStockPriceRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.accountDailyReturnRepository = accountDailyReturnRepository;
    }

    @Bean
    public Job AccountTwrDailyJob(JobRepository jobRepository, Step accountTwrDailyStep) {
        return new JobBuilder("AccountTwrDailyJob", jobRepository)
                .start(accountTwrDailyStep)
                .build();
    }

    @Bean
    public Step accountTwrDailyStep(JobRepository jobRepository, PlatformTransactionManager tm) {
        return new StepBuilder("accountTwrDailyStep", jobRepository)
                .<Member, AccountDailyReturn>chunk(50, tm)
                .reader(memberReaderForAccounts())
                .processor(accountTwrProcessor())
                .writer(accountTwrWriter())
                .faultTolerant().retryLimit(3).retry(Exception.class).skipLimit(50).skip(Exception.class)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Member> memberReaderForAccounts() {
        return new JpaPagingItemReaderBuilder<Member>()
                .name("memberReaderForAccounts")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(10)
                .queryString("SELECT m FROM Member m")
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, AccountDailyReturn> accountTwrProcessor() {
        return member -> {
            List<BankAccount> accounts = bankAccountRepository.findAllByMemberId(member.getId());
            ExchangeRate fx = exchangeRateRepository.findFirstByOrderByIdDesc();
            BigDecimal navBegin = BigDecimal.ZERO;
            BigDecimal navEnd = BigDecimal.ZERO;
            Long accountId = null;
            if (accounts.isEmpty()) return null;
            BankAccount account = accounts.get(0);
            accountId = account.getId();

            Optional<AccountDailyReturn> prevOpt = accountDailyReturnRepository.findTopByAccountIdAndDateLessThanOrderByDateDesc(accountId, LocalDate.now());
            navBegin = prevOpt.map(AccountDailyReturn::getNavEnd).orElse(BigDecimal.ZERO);

            for (Stock s : account.getStocks()) {
                Stocks meta = stocksRepository.findBySymbol(s.getSymbol()).orElseThrow(() -> new RuntimeException("NOT FOUND SYMBOL"));
                if ("KR".equals(meta.getNational())) {
                    KrNowStockPrice p = krNowStockPriceRepository.findById(s.getSymbol()).orElse(null);
                    if (p != null) navEnd = navEnd.add(s.getQuantity().multiply(p.getStck_prpr()));
                } else {
                    OverSeaNowStockPrice p = overSeaNowStockPriceRepository.findById("D".concat(meta.getCode()).concat(s.getSymbol())).orElse(null);
                    if (p != null && fx != null) navEnd = navEnd.add(s.getQuantity().multiply(p.getLast()).multiply(fx.getBasePrice()));
                }
            }

            BigDecimal netFlow = BigDecimal.ZERO;
            BigDecimal daily = BigDecimal.ZERO;
            if (navBegin != null && navBegin.signum() != 0) {
                daily = navEnd.subtract(navBegin).subtract(netFlow)
                        .divide(navBegin, 10, java.math.RoundingMode.HALF_UP);
            }

            BigDecimal cum = prevOpt.map(AccountDailyReturn::getCumIndex).orElse(BigDecimal.valueOf(100));
            cum = cum.multiply(BigDecimal.ONE.add(daily));

            AccountDailyReturn e = new AccountDailyReturn();
            e.setDate(LocalDate.now());
            e.setAccountId(accountId);
            e.setNavBegin(navBegin);
            e.setNavEnd(navEnd);
            e.setNetFlow(netFlow);
            e.setDailyTwr(daily);
            e.setCumIndex(cum);
            return e;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<AccountDailyReturn> accountTwrWriter() {
        return list -> {
            for (AccountDailyReturn e : list) {
                accountDailyReturnRepository.upsert(e);
            }
        };
    }
}


