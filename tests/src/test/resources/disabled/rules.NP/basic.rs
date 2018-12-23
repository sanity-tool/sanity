use std::ptr;

unsafe fn test() {
        let p: *const i32 = ptr::null();
        std::ptr::read(p);
}
