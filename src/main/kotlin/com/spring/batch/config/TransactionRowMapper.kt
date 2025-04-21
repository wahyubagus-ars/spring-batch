package com.spring.batch.config

import com.spring.batch.domain.dto.Transaction
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

class TransactionRowMapper : RowMapper<Transaction> {

    override fun mapRow(rs: ResultSet, rowNum: Int): Transaction? {
        return rs.getTimestamp("transaction_date")?.toLocalDateTime()?.let {
            Transaction(
                id = rs.getLong("id"),
                accountNumber = rs.getString("account_number"),
                amount = rs.getBigDecimal("amount"),
                transactionDate = it
            )
        }
    }
}
