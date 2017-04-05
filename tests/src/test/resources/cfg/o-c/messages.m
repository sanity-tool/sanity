@interface MyObject
- (void)foo;
- (void)useSelf;
@end

@implementation MyObject
- (void)useSelf {
    [self foo];
}
@end

@interface MyChild : MyObject
@end

@implementation MyChild
- (void)foo {
    [super foo];
}
@end

MyObject *OBJ;

static void testNew() {
    OBJ = [MyObject new];
}

static void testAllocInit() {
    OBJ = [[MyObject alloc] init];
}