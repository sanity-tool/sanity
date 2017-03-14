static void (^SIMPLE)(void);

extern void function();

static void defineBlock() {
    SIMPLE = ^{
        function();
    };
}

static void callBlock() {
    SIMPLE();
}

static void testBlockVars() {
    __block int count = 0;
    SIMPLE = ^{
        count++;
    };
}