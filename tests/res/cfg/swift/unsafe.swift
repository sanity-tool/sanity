func testReadFromUnsafe(p: UnsafePointer<Int>) {
    var v = p.memory;
}

func testWriteToUnsafe(p: UnsafeMutablePointer<Int>) {
    p.memory = 1;
}
