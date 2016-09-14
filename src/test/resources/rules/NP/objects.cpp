struct Foo {
    ~Foo();
    void bar();
    virtual void baz();
} *GPFOO;

static void testNonVirtual() {
    GPFOO = 0;
    GPFOO->bar();
}

static void testVirtual() {
    GPFOO = 0;
    GPFOO->baz();
}