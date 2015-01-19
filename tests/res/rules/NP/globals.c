int *GLOBAL;

void testSimple() {
	GLOBAL = 0;
	*GLOBAL = 1;
}

void testSwitch() {
    GLOBAL = 0;
    switch (*GLOBAL) {
    }
}

void testCondition() {
    GLOBAL = 0;
    if (*GLOBAL);
}