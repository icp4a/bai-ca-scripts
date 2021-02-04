# NLQ Import Schema

The decision service schema is described in a JSON format structures in 3 parts:
- the metadata,
- the signature,
- the concepts.

The data below shows a decision service descriptor sample.

```console
{
   "version":"0.9.1",
   "assetPath":"/test_deployment/1.0/loan_validation_with_score_and_grade/1.0",
   "timestamp":null,
   "signature":{
      "parameters":[
         {
            "name":"borrower",
            "verbalization":"the borrower",
            "direction":"input",
            "type":"loan.borrower"
         },
        ...
         {
            "name":"grade",
            "verbalization":"the grade",
            "direction":"output",
            "type":"java.lang.integer"
         },
      ]
   },
   "concepts":[
      {
         "name":"loan.Report",
         "label":"report",
         "pluralLabel":"reports",
         "attributes":[
            {
               "name":"loan.Report.insuranceRate",
               "type":"double",
               "roleLabel":"insurance rate",
               "pluralRoleLabel":"insurance rates",
               "verbalizationExample":"the insurance rate, an insurance rate, the insurance rates..."
            },
            ...
         ],
         "constants":[

         ]
      } ]
   }
```
