@interface MyObjectWithProperty

@property int intProperty;

- (int)readSelf;

@end

@implementation MyObjectWithProperty

- (int)readSelf {
    return self.intProperty;
}

@end

extern MyObjectWithProperty *OBJ;

static int readGlobal() {
    return OBJ.intProperty;
}
