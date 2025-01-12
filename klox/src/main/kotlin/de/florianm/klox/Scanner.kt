package de.florianm.klox

class Scanner(
    private val source: String
) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd() = source.length <= current

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GRATER_EQUAL else TokenType.GRATER)
            '/' -> {
                if (match('/')) {
                    while ('\n' != peek() && !isAtEnd()) advance()
                } else if(match('*')){
                    multiLineComment()
                } else {
                    addToken(TokenType.SLASH)
                }
            }

            ' ', '\t', '\r' -> {} // ignore whitespaces
            '\n' -> line++
            '"' -> string()
            else -> {
                if (isDigit(c)) {
                    number()
                } else if (isAlpha(c)) {
                    identifier()
                } else {
                    KLox.error(line, "Unexpected character.")
                }
            }
        }
    }

    private fun advance() = source[current++]

    private fun addToken(tokenType: TokenType) {
        addToken(tokenType, null)
    }

    private fun addToken(
        tokenType: TokenType,
        literal: Any?
    ) {
        val text = source.substring(start until current)
        tokens.add(Token(tokenType, text, literal, line))
    }

    private fun match(
        character: Char
    ): Boolean {
        if (isAtEnd()) return false
        if (source[current] != character) return false

        current++
        return true
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[current]
    }

    private fun string() {
        while ('"' != peek() && !isAtEnd()) {
            if ('\n' == peek()) line++
            advance()
        }

        if (isAtEnd()) {
            KLox.error(line, "Unterminated string.")
        }

        // closing "
        advance()

        val value = source.substring(start + 1 until current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun number() {
        while (isDigit(peek())) {
            advance()
        }

        if ('.' == peek() && isDigit(peekNext())) {
            // consume '.'
            advance()

            while (isDigit(peek())) advance()
        }

        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private fun isDigit(value: Char) = value in '0'..'9'

    private fun peekNext(): Char {
        if (current + 1 >= source.length) {
            return '\u0000'
        }

        return source[current + 1]
    }

    private fun isAlpha(value: Char) = value in 'a'..'z' || value in 'A'..'Z' || value == '_'

    private fun isAlphaNumeric(value: Char) = isAlpha(value) || isDigit(value)

    private fun identifier() {
        while (isAlphaNumeric(peek())) {
            advance()
        }

        val identifier = source.substring(start, current)
        addToken(reservedWord.getOrDefault(identifier, TokenType.IDENTIFIER))
    }

    private fun multiLineComment(){
        var level = 1
        while(0 < level && !isAtEnd()){
            if ('\n' == peek()) line++
            println("peek=${peek()}, peekNext=${peekNext()}")

            if('*' == peek() && '/' == peekNext()){
                advance()
                level--
            } else if('/' == peek() && '*' == peekNext()){
                advance()
                level++
            }

            advance()
        }

        if (0 < level) {
            KLox.error(line, "Unterminated multiline comment.")
        }
    }

    companion object {
        val reservedWord = mapOf(
            "and" to TokenType.AND,
            "class" to TokenType.CLASS,
            "else" to TokenType.ELSE,
            "false" to TokenType.FALSE,
            "fun" to TokenType.FUN,
            "for" to TokenType.FOR,
            "if" to TokenType.IF,
            "nil" to TokenType.NIL,
            "or" to TokenType.OR,
            "print" to TokenType.PRINT,
            "return" to TokenType.RETURN,
            "super" to TokenType.SUPER,
            "true" to TokenType.TRUE,
            "var" to TokenType.VAR,
            "while" to TokenType.WHILE
        )
    }
}
