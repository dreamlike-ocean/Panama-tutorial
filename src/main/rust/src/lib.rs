#[no_mangle]
pub extern "C" fn add(i: i32, j: i32) -> i32 {
    i + j
}

#[no_mangle]
pub extern "C" fn callback(i: i32, j: i32, f: extern fn(i32, i32) -> i32) -> i32 {
    i + j + f(i, j)
}

#[repr(C)]
pub struct Person {
    pub a: i32,
    pub n: i64,
}

#[repr(C)]
pub union int_or_long {
    pub int_value: i32,
    pub long_value: i64,
}

#[repr(C)]
pub struct long_and_long {
    pub a: i64,
    pub b: i64,
}

#[repr(C)]
pub struct Complex {
    pub a: i64,
    pub long_array: [i64; 3],
    pub sub_struct: long_and_long,
    pub long_array_ptr: *mut i64,
    pub long_and_long_ptr: *mut long_and_long,
}

#[repr(C)]
pub struct big_struct {
    pub a: i64,
    pub b: i64,
    pub c: i64,
    pub d: i64,
}

#[no_mangle]
pub extern "C" fn complex_size() -> usize {
    std::mem::size_of::<Complex>()
}

#[no_mangle]
pub extern "C" fn person_size() -> usize {
    return std::mem::size_of::<Person>();
}

#[no_mangle]
pub extern "C" fn get_b(big_struct: big_struct) -> i64 {
    big_struct.b
}

#[no_mangle]
pub unsafe extern "C" fn get_array_by_index(java_heap_array: *mut i64, index: i32) -> i64 {
    java_heap_array.add(index as usize).read()
}


#[no_mangle]
pub extern "C" fn new_big_struct() -> big_struct {
    big_struct {
        a: 1,
        b: 2,
        c: 3,
        d: 4,
    }
}


#[no_mangle]
pub unsafe extern "C" fn error_syscall(errono: usize) -> i32 {
    libc::__errno_location().write(-(errono as i32));
     -1
}