# Loan validation with grade and score

## Purpose
Used for testing and simulation:
Rules from  projects : Check, Determination, Scoring
with no extraction mechanism.

## Interface
- input a loan request and a borrower
- output the loan report, the grade, and the score.

- Input
  - parameter
    - name: borrower
    - verbalization: the borrower
    - type: loan.borrower
    - initial value: none
  - parameter
    - name: loan
    - verbalization: the loan
    - loan.Loan
    - initial value: none
    
- Output
  - parameter
    - name: report
    - verbalization: the loan report
    - type: loan.Report
  - parameter
    - name: score
    - verbalization: the score
    - type: java.lang.integer
  - parameter
    - name: grade
    - verbalization: the grade
    - type: java.lang.String
