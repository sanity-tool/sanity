@interface MyUtility
+ (void)voidUtility;
+ (int)intUtility;
+ (void)voidUtilityWithParam:(int) param withParam2:(int)param2;
@end

extern int R_INT;
static void sendStaticMessages() {
    [MyUtility voidUtility];
    R_INT = [MyUtility intUtility];
    [MyUtility voidUtilityWithParam:1 withParam2: 2];
}