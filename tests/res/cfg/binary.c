static int IR, I0, I1;

static void testInt() {
    IR = I0 + I1;
    IR = I0 - I1;
    IR = I0 * I1;
    IR = I0 / I1;
    IR = I0 % I1;
    IR = I0 & I1;
    IR = I0 | I1;
    IR = I0 ^ I1;
    IR = I0 << I1;
    IR = I0 >> I1;
}

static void testIntCmp() {
    IR = I0 < I1;
    IR = I0 > I1;
    IR = I0 <= I1;
    IR = I0 >= I1;
    IR = I0 == I1;
    IR = I0 != I1;
}