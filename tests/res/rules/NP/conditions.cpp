bool *GPBOOL;

static void testNullAssign() {
    GPBOOL = 0;
    if (*GPBOOL);
}

static void testNullCheck() {
    if (GPBOOL);
    if (*GPBOOL);
}