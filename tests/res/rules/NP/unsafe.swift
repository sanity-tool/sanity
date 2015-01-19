func testReadFromUnsafe() {
    var p: UnsafePointer<Int> = nil;
    var v = p.memory;
}

func testWriteToUnsafe() {
    var p: UnsafeMutablePointer<Int> = nil;
    p.memory = 1;
}
