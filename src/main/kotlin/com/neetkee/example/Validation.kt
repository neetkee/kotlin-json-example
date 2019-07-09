package com.neetkee.example

class ValidationError(val violations: List<Violation>)
class Violation(val field: String, val message: String)