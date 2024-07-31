use std::{
    ffi::{c_char, c_int, CString},
    slice,
};

/// Writes the current package version as a null-terminated C-style string to the provided buffer.
///
/// # Arguments
/// * `buffer` - A mutable pointer to a buffer that will receive the version string.
/// * `len` - The length of the buffer in bytes.
///
/// # Returns
/// - If `buffer` is `null`, returns the required length of the buffer (including the null terminator).
/// - If the buffer is too small, returns `-1`.
/// - Otherwise, writes the version string to the buffer and returns the length of the version string (including the null terminator).
#[no_mangle]
pub extern "C" fn version(buffer: *mut u8, len: usize) -> isize {
    let version = env!("CARGO_PKG_VERSION");
    let version_bytes = version.as_bytes();
    let required_len = version_bytes.len() + 1; // +1 for null terminator

    if buffer.is_null() {
        return required_len as isize;
    }

    if len < version_bytes.len() {
        return -1;
    }

    unsafe {
        let buffer_slice = slice::from_raw_parts_mut(buffer, required_len);
        buffer_slice[..version_bytes.len()].copy_from_slice(version_bytes);
        buffer_slice[version_bytes.len()] = 0; // null terminator
    }

    required_len as isize
}

type Callback = unsafe extern "C" fn(*const c_char, c_int);

/// Calls the provided callback function with the current package version as a C-style string.
///
/// The callback function will be called with the following arguments:
/// - `*const c_char`: A pointer to a null-terminated C-style string containing the package version.
/// - `c_int`: The length of the version string, including the null terminator.
///
/// This function is marked as `unsafe` because it calls the provided callback function, which may perform unsafe operations.
#[no_mangle]
pub unsafe extern "C" fn versionString(callback: Callback) {
    let version = env!("CARGO_PKG_VERSION");
    let c_string = CString::new(version).expect("CString::new failed");
    callback(c_string.as_ptr(), (version.len() + 1) as c_int);
}

#[cfg(test)]
mod tests {
    use super::*;
}
