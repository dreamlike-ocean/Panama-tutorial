rm -f ../resources/libperson.so
cargo build
mkdir -p ../resources
mv target/debug/libperson.so ../../../libperson.so