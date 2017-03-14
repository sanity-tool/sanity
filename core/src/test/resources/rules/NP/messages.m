@interface MyObject
- (void)foo;
@end

static void test() {
    MyObject *OBJ = 0;
    [OBJ foo];
}