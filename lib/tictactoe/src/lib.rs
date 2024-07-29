use std::{ffi::{c_char, CString}, slice};

#[no_mangle]
pub extern "C" fn add(left: u64, right: u64) -> u64 {
    left + right
}

#[no_mangle]
pub extern "C" fn version(buffer: *mut u8, len: usize) -> isize {
    let version = env!("CARGO_PKG_VERSION");
    let version_bytes = version.as_bytes();
    let required_len = version_bytes.len() + 1; // +1 for null terminator

    if buffer.is_null() {
        return version_bytes.len() as isize;
    }

    if len < version_bytes.len() {
        return -1;
    }

    unsafe {
        let buffer_slice = slice::from_raw_parts_mut(buffer, len);
        buffer_slice[..version_bytes.len()].copy_from_slice(version_bytes);
        buffer_slice[version_bytes.len()] = 0; // null terminator
    }

    required_len as isize
}

type Callback = unsafe extern fn(*const c_char);

#[no_mangle]
pub unsafe extern fn versionString(callback: Callback) {
    let version = env!("CARGO_PKG_VERSION");
    let c_string = CString::new(version).expect("CString::new failed");
    callback(c_string.as_ptr())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_works() {
        let result = add(2, 2);
        assert_eq!(result, 4);
    }
}
