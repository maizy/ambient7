package ru.maizy.influxdbclient.responses

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

// FIXME: implements
object QueryResultsProtocol extends ErrorProtocol

// ex.
//  {
//    "results": [
//      {
//        "series": [
//          {
//            "name": "co2",
//            "columns": [
//              "time",
//              "max"
//            ],
//            "values": [
//              [
//                "2015-11-06T00:00:00Z",
//                802
//              ]
//            ]
//          }
//        ]
//      }
//    ]
//  }
