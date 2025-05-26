# Technical Specification

## Purpose

The purpose of this PL/SQL script is to create three tables in an Oracle database: `Transactions`, `MarketData`, and `PortfolioHoldings`. These tables are designed to store financial data related to transactions, market data, and portfolio holdings respectively. The script also creates indexes on these tables to optimize performance.

## Inputs & Outputs

The script does not take any inputs as it is a Data Definition Language (DDL) script. The output of the script is the creation of the three tables and their associated indexes in the database.

## Main Logic or Flow

1. **Transactions Table**: This table is created with columns to store transaction details such as transaction id, portfolio id, asset id, transaction date, transaction type, quantity, amount, price, and currency. The transaction type can only be 'BUY', 'SELL', 'DIVIDEND', or 'FEE'. A check constraint is added to ensure that quantity is not null for 'BUY' and 'SELL' transactions and is null for 'DIVIDEND' and 'FEE' transactions.

2. **MarketData Table**: This table is created with columns to store market data such as asset id, date, price, currency, and exchange rate. The primary key for this table is a combination of asset id and date.

3. **PortfolioHoldings Table**: This table is created with columns to store portfolio holdings data such as portfolio id, asset id, and quantity. The primary key for this table is a combination of portfolio id and asset id.

4. **Indexes**: Three indexes are created for performance optimization. The `idx_transactions_portfolio` index is created on the portfolio_id column of the Transactions table. The `idx_marketdata_asset_date` index is created on the asset_id and date columns of the MarketData table. The `idx_portfolioholdings_portfolio` index is created on the portfolio_id column of the PortfolioHoldings table.

## Error Handling

The script does not include explicit error handling. However, Oracle will return an error if there is an attempt to create a table or index that already exists, or if there is a violation of any of the constraints specified in the table definitions.

## Dependencies

The script does not have any dependencies. It is a standalone script that creates tables and indexes in an Oracle database. However, the successful execution of this script requires sufficient privileges to create tables and indexes in the target database.

---------------------------------------------


--- Technical Specification for: Functions_script.sql ---
# Technical Specification

## 1. GetAssetValueInBaseCurrency Function

### Purpose
This function calculates the value of an asset in a specified base currency.

### Inputs
- `p_asset_id`: The ID of the asset.
- `p_quantity`: The quantity of the asset.
- `p_date`: The date for which the asset value is to be calculated.
- `p_base_currency`: The base currency in which the asset value is to be calculated.

### Outputs
- Returns the value of the asset in the base currency.

### Main Logic
- The function fetches the price, exchange rate, and currency of the asset from the `MarketData` table for the given asset ID and date.
- If the currency of the asset is the same as the base currency, the value of the asset in the base currency is calculated as the product of the quantity and the price.
- If the currency of the asset is different from the base currency, the value of the asset in the base currency is calculated as the product of the quantity, the price, and the exchange rate.

### Error Handling
- If no data is found in the `MarketData` table for the given asset ID and date, a message is printed and the function returns 0.
- If any other error occurs, a message is printed with the error details and the function returns 0.

### Dependencies
- This function depends on the `MarketData` table.

## 2. CalculateDividendsAndFees Function

### Purpose
This function calculates the total dividends and fees for a portfolio in a specified base currency within a given date range.

### Inputs
- `p_portfolio_id`: The ID of the portfolio.
- `p_start_date`: The start date of the period.
- `p_end_date`: The end date of the period.
- `p_base_currency`: The base currency in which the dividends and fees are to be calculated.

### Outputs
- Returns the net amount of dividends and fees (total dividends minus total fees) in the base currency.

### Main Logic
- The function fetches all transactions for the given portfolio ID within the specified date range from the `Transactions` table.
- For each transaction, it calculates a currency factor based on the transaction currency and the base currency.
- If the transaction type is 'DIVIDEND', the amount is added to the total dividends.
- If the transaction type is 'FEE', the amount is added to the total fees.
- The net amount of dividends and fees is returned.

### Error Handling
- If any error occurs, a message is printed with the error details and the function returns 0.

### Dependencies
- This function depends on the `Transactions` and `MarketData` tables.

## 3. CalculatePortfolioValue Function

### Purpose
This function calculates the total value of a portfolio in a specified base currency on a given date.

### Inputs
- `p_portfolio_id`: The ID of the portfolio.
- `p_date`: The date for which the portfolio value is to be calculated.
- `p_base_currency`: The base currency in which the portfolio value is to be calculated.

### Outputs
- Returns the total value of the portfolio in the base currency.

### Main Logic
- The function fetches all assets in the portfolio from the `PortfolioHoldings` table.
- For each asset, it calculates the value in the base currency using the `GetAssetValueInBaseCurrency` function and adds it to the total value.

### Error Handling
- If any error occurs, a message is printed with the error details and the function returns 0.

### Dependencies
- This function depends on the `PortfolioHoldings` table and the `GetAssetValueInBaseCurrency` function.

---------------------------------------------


--- Technical Specification for: SProc_script.sql ---
# Specification for CalculatePortfolioPerformance Procedure

## Purpose
The `CalculatePortfolioPerformance` procedure is designed to calculate the performance and risk metric of a given portfolio over a specified period. The performance is calculated as the total return (final value plus dividends and fees minus initial value) as a percentage of the initial value. The risk metric is a simplified calculation of the change in value over the initial value.

## Inputs
The procedure takes the following inputs:
- `p_portfolio_id` (Number): The ID of the portfolio for which the performance is to be calculated.
- `p_start_date` (Date): The start date of the period over which the performance is to be calculated.
- `p_end_date` (Date): The end date of the period over which the performance is to be calculated.
- `p_base_currency` (VARCHAR2): The base currency in which the portfolio values are to be calculated.

## Outputs
The procedure provides the following outputs:
- `p_performance` (Number): The calculated performance of the portfolio over the specified period.
- `p_risk_metric` (Number): The calculated risk metric of the portfolio over the specified period.

## Main Logic or Flow
1. The procedure first calculates the initial and final values of the portfolio at the start and end dates, respectively, using the `CalculatePortfolioValue` function.
2. It then calculates the total dividends and fees over the period using the `CalculateDividendsAndFees` function.
3. The total return is calculated as the sum of the final value and total dividends and fees, minus the initial value.
4. If the initial value is greater than zero, the performance is calculated as the total return divided by the initial value, multiplied by 100. If the initial value is zero or less, the performance is set to NULL.
5. The risk metric is calculated as the change in value (final value minus initial value) divided by the initial value.
6. A message is output to indicate that the performance has been calculated successfully.

## Error Handling
If any error occurs during the execution of the procedure, the `p_performance` and `p_risk_metric` outputs are set to NULL, and an error message is output with the text of the error.

## Dependencies
The procedure depends on the following functions:
- `CalculatePortfolioValue`: This function calculates the value of a portfolio at a given date in a specified base currency.
- `CalculateDividendsAndFees`: This function calculates the total dividends and fees for a portfolio over a specified period in a specified base currency.
