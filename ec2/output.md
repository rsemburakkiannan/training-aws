========= Specifications for: DDL_script.sql =========

--- FUNCTIONAL_SPEC ---

Functional Specification:

Business Purpose:
The Oracle PL/SQL code is designed to create and manage a database for a financial trading system. The database will store and manage transaction data, market data, and portfolio holdings for different assets and portfolios.

High-Level Business Logic:
The business logic is centered around the management of financial transactions, market data, and portfolio holdings. The Transactions table stores all the transactions made, the MarketData table stores the price and exchange rate of different assets on different dates, and the PortfolioHoldings table stores the quantity of different assets held in different portfolios.

Business Rules:
1. Transactions can be of type 'BUY', 'SELL', 'DIVIDEND', or 'FEE'.
2. For 'BUY' and 'SELL' transactions, the quantity must not be null.
3. For 'DIVIDEND' and 'FEE' transactions, the quantity must be null.
4. Each asset's price, currency, and exchange rate on a specific date are stored in the MarketData table.
5. The quantity of each asset in each portfolio is stored in the PortfolioHoldings table.

Input and Output Overview:
The input to this system would be the transaction data (including transaction type, quantity, amount, price, and currency), market data (including asset price, currency, and exchange rate on a specific date), and portfolio holdings data (including portfolio id, asset id, and quantity). The output would be the data stored in the three tables, which can be used for various financial analyses and reporting.

Assumptions:
1. The transaction_id in the Transactions table is unique for each transaction.
2. The asset_id and date in the MarketData table together form a unique identifier for each record.
3. The portfolio_id and asset_id in the PortfolioHoldings table together form a unique identifier for each record.
4. The indexes created on portfolio_id in the Transactions and PortfolioHoldings tables, and on asset_id and date in the MarketData table, are for performance optimization. They are assumed to significantly speed up data retrieval operations.

--- PSEUDOCODE_SPEC ---

Pseudocode:

1. Define a function to create a table named "Transactions" with the following fields:
   - transaction_id: a unique identifier for each transaction
   - portfolio_id: an identifier for the portfolio involved in the transaction
   - asset_id: an identifier for the asset involved in the transaction
   - transaction_date: the date of the transaction
   - transaction_type: the type of transaction, which can be 'BUY', 'SELL', 'DIVIDEND', or 'FEE'
   - quantity: the quantity of the asset involved in the transaction
   - amount: the amount of the transaction
   - price: the price of the asset at the time of the transaction
   - currency: the currency used in the transaction
   - A constraint to ensure that if the transaction type is 'BUY' or 'SELL', the quantity must not be null, and if the transaction type is 'DIVIDEND' or 'FEE', the quantity must be null.

2. Define a function to create a table named "MarketData" with the following fields:
   - asset_id: an identifier for the asset
   - date: the date of the market data
   - price: the price of the asset on the given date
   - currency: the currency of the price
   - exchange_rate: the exchange rate on the given date
   - The primary key is a combination of asset_id and date.

3. Define a function to create a table named "PortfolioHoldings" with the following fields:
   - portfolio_id: an identifier for the portfolio
   - asset_id: an identifier for the asset
   - quantity: the quantity of the asset in the portfolio
   - The primary key is a combination of portfolio_id and asset_id.

4. Define a function to create an index on the "Transactions" table for the portfolio_id field to optimize performance.

5. Define a function to create an index on the "MarketData" table for the asset_id and date fields to optimize performance.

6. Define a function to create an index on the "PortfolioHoldings" table for the portfolio_id field to optimize performance.

--- TECHNICAL_SPEC ---

Technical Specification:

Module Purpose:
The purpose of this module is to create a database structure for storing and managing financial transactions, market data, and portfolio holdings. The module includes the creation of three tables: Transactions, MarketData, and PortfolioHoldings. It also includes the creation of indexes to optimize the performance of database operations.

Input/Output Variables:
There are no input or output variables in this module as it is a Data Definition Language (DDL) script for creating tables and indexes in a database.

Key Procedures/Functions:
The key procedures in this module are the CREATE TABLE and CREATE INDEX commands. The CREATE TABLE command is used to create the Transactions, MarketData, and PortfolioHoldings tables with their respective columns and constraints. The CREATE INDEX command is used to create indexes on the portfolio_id column in the Transactions and PortfolioHoldings tables, and on the asset_id and date columns in the MarketData table.

Exception Handling:
There is no explicit exception handling in this module. However, the Oracle database system will automatically raise an error if there is an attempt to create a table or index that already exists, or if there is a violation of any of the constraints defined in the CREATE TABLE commands.

Dependencies:
This module does not have any explicit dependencies. However, it assumes that the Oracle database system is already installed and properly configured. It also assumes that the user running this script has the necessary permissions to create tables and indexes in the database.

--- DESIGNS_FROM_SPECS ---

1. Activity Diagram Description:
   The activity diagram would start with the creation of three tables: Transactions, MarketData, and PortfolioHoldings. The Transactions table would have fields like transaction_id, portfolio_id, asset_id, transaction_date, transaction_type, quantity, amount, price, and currency. The MarketData table would have fields like asset_id, date, price, currency, and exchange_rate. The PortfolioHoldings table would have fields like portfolio_id, asset_id, and quantity. After the creation of these tables, indexes would be created on certain fields of these tables for performance optimization. The indexes would be created on portfolio_id in Transactions table, asset_id and date in MarketData table, and portfolio_id in PortfolioHoldings table.

2. Database Schema Diagram Description:
   The database schema diagram would consist of three tables: Transactions, MarketData, and PortfolioHoldings. The Transactions table would be linked to the PortfolioHoldings table through the portfolio_id field and to the MarketData table through the asset_id field. The MarketData table would also be linked to the PortfolioHoldings table through the asset_id field. The Transactions table would have a primary key of transaction_id, the MarketData table would have a composite primary key of asset_id and date, and the PortfolioHoldings table would have a composite primary key of portfolio_id and asset_id.

3. Process Flow Diagram Description:
   The process flow diagram would start with the creation of the Transactions, MarketData, and PortfolioHoldings tables. After the tables are created, data would be inserted into these tables. The next step would be the creation of indexes on certain fields of these tables for performance optimization. The indexes would be created on portfolio_id in Transactions table, asset_id and date in MarketData table, and portfolio_id in PortfolioHoldings table. The process flow would end with the querying of data from these tables.

=================================================


========= Specifications for: Functions_script.sql =========

--- FUNCTIONAL_SPEC ---

Functional Specification:

1. Business Purpose:
The provided PL/SQL code contains three functions that are used to calculate the value of assets, dividends, fees, and the total value of a portfolio. These functions are essential for financial analysis and portfolio management.

2. High-Level Business Logic:
- GetAssetValueInBaseCurrency: This function calculates the value of an asset in a base currency. It fetches the price, exchange rate, and currency of the asset from the MarketData table. If the asset's currency is the same as the base currency, the value is calculated by multiplying the quantity with the price. Otherwise, the exchange rate is also factored in.
- CalculateDividendsAndFees: This function calculates the total dividends and fees for a portfolio within a specified date range. It fetches transaction data from the Transactions table and calculates the total dividends and fees in the base currency.
- CalculatePortfolioValue: This function calculates the total value of a portfolio on a specific date. It fetches the asset ID and quantity from the PortfolioHoldings table and uses the GetAssetValueInBaseCurrency function to calculate the total value.

3. Business Rules:
- The asset value is calculated in the base currency.
- The total dividends and fees are calculated for a specific portfolio within a specified date range.
- The portfolio value is calculated for a specific date.

4. Input and Output Overview:
- GetAssetValueInBaseCurrency: Inputs are asset ID, quantity, date, and base currency. The output is the asset value in the base currency.
- CalculateDividendsAndFees: Inputs are portfolio ID, start date, end date, and base currency. The output is the total dividends minus total fees in the base currency.
- CalculatePortfolioValue: Inputs are portfolio ID, date, and base currency. The output is the total portfolio value in the base currency.

5. Assumptions:
- The MarketData table contains the price, exchange rate, and currency for each asset on each date.
- The Transactions table contains transaction data (type, amount, currency, date) for each portfolio.
- The PortfolioHoldings table contains the asset ID and quantity for each portfolio.
- The base currency is provided as an input to each function.
- If no data is found or an error occurs during execution, the functions return 0 and print an error message.

--- PSEUDOCODE_SPEC ---

Pseudocode:

Function GetAssetValueInBaseCurrency:
- Input: asset_id (number), quantity (number), date (date), base_currency (string)
- Output: value_in_base (number)

1. Initialize variables: price (number), exchange_rate (number), currency (string), value_in_base (number)
2. Fetch price, exchange_rate, currency from MarketData where asset_id equals input asset_id and date equals input date
3. If no data found, print error message and return 0
4. If currency equals base_currency, calculate value_in_base as quantity times price
5. If currency does not equal base_currency, calculate value_in_base as quantity times price times exchange_rate
6. Return value_in_base
7. If any other error occurs, print error message and return 0

Function CalculateDividendsAndFees:
- Input: portfolio_id (number), start_date (date), end_date (date), base_currency (string)
- Output: total_dividends minus total_fees (number)

1. Initialize variables: total_dividends (number), total_fees (number), currency_factor (number)
2. For each record in Transactions where portfolio_id equals input portfolio_id and transaction_date is between start_date and end_date:
   - If currency equals base_currency, set currency_factor to 1
   - If currency does not equal base_currency, fetch exchange_rate from MarketData where currency equals record's currency and date equals record's transaction_date
   - If transaction_type equals 'DIVIDEND', add amount times currency_factor to total_dividends
   - If transaction_type equals 'FEE', add amount times currency_factor to total_fees
3. Return total_dividends minus total_fees
4. If any error occurs, print error message and return 0

Function CalculatePortfolioValue:
- Input: portfolio_id (number), date (date), base_currency (string)
- Output: total_value (number)

1. Initialize variable: total_value (number)
2. For each record in PortfolioHoldings where portfolio_id equals input portfolio_id:
   - Add the result of GetAssetValueInBaseCurrency with parameters asset_id, quantity, date, base_currency to total_value
3. Return total_value
4. If any error occurs, print error message and return 0

--- TECHNICAL_SPEC ---

Technical Specification:

Module Purpose:
The module contains three functions: GetAssetValueInBaseCurrency, CalculateDividendsAndFees, and CalculatePortfolioValue. These functions are used to calculate the value of an asset in a base currency, calculate the total dividends and fees for a portfolio within a specific date range, and calculate the total value of a portfolio on a specific date, respectively.

Input/Output Variables:
1. GetAssetValueInBaseCurrency:
   - Input: p_asset_id (Number), p_quantity (Number), p_date (Date), p_base_currency (String)
   - Output: v_value_in_base (Number)

2. CalculateDividendsAndFees:
   - Input: p_portfolio_id (Number), p_start_date (Date), p_end_date (Date), p_base_currency (String)
   - Output: v_total_dividends - v_total_fees (Number)

3. CalculatePortfolioValue:
   - Input: p_portfolio_id (Number), p_date (Date), p_base_currency (String)
   - Output: v_total_value (Number)

Key Procedures/Functions:
1. GetAssetValueInBaseCurrency: This function calculates the value of an asset in a base currency.
2. CalculateDividendsAndFees: This function calculates the total dividends and fees for a portfolio within a specific date range.
3. CalculatePortfolioValue: This function calculates the total value of a portfolio on a specific date.

Exception Handling:
Each function has exception handling to catch any errors that occur during execution. If an error occurs, a message is output to the console and the function returns 0.

Dependencies:
The functions depend on the following database tables:
1. MarketData: This table contains the price, exchange rate, and currency for each asset on a specific date.
2. Transactions: This table contains the transaction type, amount, currency, and transaction date for each transaction in a portfolio.
3. PortfolioHoldings: This table contains the asset id and quantity for each asset in a portfolio.

The CalculatePortfolioValue function also depends on the GetAssetValueInBaseCurrency function.

--- DESIGNS_FROM_SPECS ---

1. Activity Diagram Description:
   - The activity diagram would start with the initiation of the functions GetAssetValueInBaseCurrency, CalculateDividendsAndFees, and CalculatePortfolioValue.
   - For GetAssetValueInBaseCurrency, the activity would involve fetching price, exchange rate, and currency from the MarketData table. Then, it would calculate the value in base currency based on whether the currency is the base currency or not. If an exception occurs, it would handle it and return 0.
   - For CalculateDividendsAndFees, the activity would involve looping through each transaction in the Transactions table for a given portfolio and date range. It would calculate the total dividends and fees based on the transaction type and currency. If an exception occurs, it would handle it and return 0.
   - For CalculatePortfolioValue, the activity would involve looping through each asset in the PortfolioHoldings table for a given portfolio. It would calculate the total value by calling the GetAssetValueInBaseCurrency function. If an exception occurs, it would handle it and return 0.
   - The activity diagram would end with the return of the calculated values from each function.

2. Database Schema Diagram Description:
   - The database schema would consist of three tables: MarketData, Transactions, and PortfolioHoldings.
   - The MarketData table would have columns for asset_id, date, price, exchange_rate, and currency.
   - The Transactions table would have columns for portfolio_id, transaction_date, transaction_type, amount, and currency.
   - The PortfolioHoldings table would have columns for portfolio_id and asset_id, and quantity.
   - The asset_id in the MarketData table would be a foreign key referencing the asset_id in the PortfolioHoldings table.
   - The portfolio_id in the Transactions table would be a foreign key referencing the portfolio_id in the PortfolioHoldings table.

3. Process Flow Diagram Description:
   - The process flow would start with the initiation of the functions GetAssetValueInBaseCurrency, CalculateDividendsAndFees, and CalculatePortfolioValue.
   - For GetAssetValueInBaseCurrency, the process would involve fetching data from the MarketData table, performing calculations, and returning the result.
   - For CalculateDividendsAndFees, the process would involve fetching data from the Transactions table, performing calculations in a loop, and returning the result.
   - For CalculatePortfolioValue, the process would involve fetching data from the PortfolioHoldings table, calling the GetAssetValueInBaseCurrency function in a loop, performing calculations, and returning the result.
   - The process flow would end with the return of the calculated values from each function.

=================================================


========= Specifications for: SProc_script.sql =========

--- FUNCTIONAL_SPEC ---

Functional Specification:

Business Purpose:
The purpose of the PL/SQL procedure 'CalculatePortfolioPerformance' is to calculate the performance and risk metric of a given portfolio over a specified period. The performance is calculated based on the initial and final values of the portfolio, dividends, and fees. The risk metric is a simplified calculation based on the change in portfolio value over the period.

High-Level Business Logic:
The procedure calculates the initial and final portfolio values using the 'CalculatePortfolioValue' function. It then calculates the total dividends and fees using the 'CalculateDividendsAndFees' function. The total return is calculated as the sum of the final value and total dividends and fees, minus the initial value. The performance is then calculated as the total return divided by the initial value, multiplied by 100. If the initial value is zero, the performance is set to NULL. The risk metric is calculated as the difference between the final and initial values, divided by the initial value.

Business Rules:
1. The performance is calculated as the total return divided by the initial value, multiplied by 100.
2. If the initial value is zero, the performance is set to NULL.
3. The risk metric is calculated as the difference between the final and initial values, divided by the initial value.

Input and Output Overview:
Inputs:
- p_portfolio_id: The ID of the portfolio.
- p_start_date: The start date of the period.
- p_end_date: The end date of the period.
- p_base_currency: The base currency for the calculations.

Outputs:
- p_performance: The calculated performance of the portfolio.
- p_risk_metric: The calculated risk metric of the portfolio.

Assumptions:
1. The 'CalculatePortfolioValue' and 'CalculateDividendsAndFees' functions are available and return the correct values.
2. The portfolio ID provided exists and is valid.
3. The start and end dates provided are valid and the end date is after the start date.
4. The base currency provided is valid.
5. The portfolio values, dividends, and fees are all in the same currency.
6. The portfolio's initial value is not zero. If it is, the performance will be set to NULL.

--- PSEUDOCODE_SPEC ---

Pseudocode:

Procedure CalculatePortfolioPerformance
  Input: p_portfolio_id (Number), p_start_date (Date), p_end_date (Date), p_base_currency (String)
  Output: p_performance (Number), p_risk_metric (Number)

  Declare v_initial_value as Number
  Declare v_final_value as Number
  Declare v_total_dividends_and_fees as Number
  Declare v_total_return as Number
  Declare v_risk_metric_calc as Number

  Begin
    // Calculate initial and final portfolio values
    Set v_initial_value = Call function CalculatePortfolioValue with parameters (p_portfolio_id, p_start_date, p_base_currency)
    Set v_final_value = Call function CalculatePortfolioValue with parameters (p_portfolio_id, p_end_date, p_base_currency)

    // Calculate dividends and fees
    Set v_total_dividends_and_fees = Call function CalculateDividendsAndFees with parameters (p_portfolio_id, p_start_date, p_end_date, p_base_currency)

    // Calculate total return
    Set v_total_return = (v_final_value + v_total_dividends_and_fees) - v_initial_value

    // Calculate performance
    If v_initial_value > 0 Then
      Set p_performance = (v_total_return / v_initial_value) * 100
    Else
      Set p_performance = NULL
    End If

    // Calculate risk metric (e.g., volatility)
    Set v_risk_metric_calc = (v_final_value - v_initial_value) / v_initial_value // Simplified risk metric
    Set p_risk_metric = v_risk_metric_calc

    Print 'Performance calculated successfully.'
  Exception
    When any error occurs Then
      Set p_performance = NULL
      Set p_risk_metric = NULL
      Print 'Error calculating performance: ' + Error Message
  End
End Procedure CalculatePortfolioPerformance

--- TECHNICAL_SPEC ---

Technical Specification:

Module Purpose:
The purpose of this module is to calculate the performance and risk metric of a given portfolio over a specified period. The performance is calculated as the total return (final value plus dividends and fees minus initial value) as a percentage of the initial value. The risk metric is calculated as the change in value (final value minus initial value) divided by the initial value.

Input Variables:
- p_portfolio_id (NUMBER): The ID of the portfolio for which the performance and risk metric are to be calculated.
- p_start_date (DATE): The start date of the period over which the performance and risk metric are to be calculated.
- p_end_date (DATE): The end date of the period over which the performance and risk metric are to be calculated.
- p_base_currency (VARCHAR2): The base currency in which the portfolio values are to be calculated.

Output Variables:
- p_performance (NUMBER): The calculated performance of the portfolio over the specified period.
- p_risk_metric (NUMBER): The calculated risk metric of the portfolio over the specified period.

Key Procedures/Functions:
- CalculatePortfolioValue: This function calculates the value of the portfolio at a given date in a specified currency.
- CalculateDividendsAndFees: This function calculates the total dividends and fees for the portfolio over a specified period in a specified currency.

Exception Handling:
In case of any exception, the procedure sets the output variables p_performance and p_risk_metric to NULL and outputs an error message with the SQL error message.

Dependencies:
This procedure depends on the following functions:
- CalculatePortfolioValue
- CalculateDividendsAndFees

These functions must be defined and available in the same schema where this procedure is created. The functions must also have the appropriate permissions to access the necessary data.

--- DESIGNS_FROM_SPECS ---

1. Activity Diagram Description:
   The activity diagram would start with the invocation of the CalculatePortfolioPerformance procedure. The procedure takes five input parameters: portfolio_id, start_date, end_date, base_currency, and two output parameters: performance and risk_metric. The first activity would be the calculation of the initial and final portfolio values using the CalculatePortfolioValue function. The next activity would be the calculation of total dividends and fees using the CalculateDividendsAndFees function. Following this, the total return is calculated. The next activity is a decision node where it checks if the initial value is greater than zero. If true, the performance is calculated, else it is set to NULL. The next activity is the calculation of the risk metric. The final activity is the output of the performance calculation. If any exception occurs during the process, the exception handling activity sets the performance and risk_metric to NULL and outputs an error message.

2. Database Schema Diagram Description:
   The database schema diagram would not be directly derived from this PL/SQL code as it does not involve any direct interaction with database tables. However, it can be inferred that there might be a 'Portfolio' table with 'portfolio_id' as a primary key. There might also be tables related to 'DividendsAndFees' and 'PortfolioValue' that are linked to the 'Portfolio' table via 'portfolio_id'. The 'PortfolioValue' table might have 'start_date', 'end_date', and 'base_currency' as attributes. The 'DividendsAndFees' table might have 'start_date', 'end_date', and 'base_currency' as attributes.

3. Process Flow Diagram Description:
   The process flow diagram would start with the input parameters being passed to the CalculatePortfolioPerformance procedure. The first process would be the calculation of initial and final portfolio values. The next process would be the calculation of total dividends and fees. The third process would be the calculation of total return. The fourth process would be a decision process to check if the initial value is greater than zero. If true, the performance is calculated, else it is set to NULL. The fifth process would be the calculation of the risk metric. The final process would be the output of the performance calculation. If any exception occurs during the process, the exception handling process sets the performance and risk_metric to NULL and outputs an error message.

=================================================
