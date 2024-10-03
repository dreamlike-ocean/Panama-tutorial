use person::Person;

#[test]
fn test_work() {
    println!("hello test!");
    assert_eq!(2 + 2, 4);
}


#[test]
fn test_sub() {
    use person::callback;
    let res = callback(1, 2, return_two);
    assert_eq!(res, 5);
}


extern fn return_two(_: i32, _: i32) -> i32 {
    2
}
