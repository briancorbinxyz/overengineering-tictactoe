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

mod tictactoe {

    #[derive(Clone)]
    pub struct GameBoard {
        dimension: u32,
        content: Vec<Vec<u32>>,
    }

    impl GameBoard {
        pub fn new(dimension: u32) -> Self {
            let mut content = Vec::new();
            for _ in 0..dimension { 
                let mut row = Vec::new(); 
                for _ in 0..dimension {
                    row.push(0);
                }
                content.push(row);
            }
            GameBoard { dimension, content }
        }

        pub fn get(&self, row: u32, col: u32) -> u32 {
            self.content[row as usize][col as usize]
        }

        pub fn get_with_index(&self, index: u32) -> u32 {
            let row = index / self.dimension;
            let col = index % self.dimension;
            self.get(row, col)
        }

        pub fn with_value_at(&self, row: u32, col: u32, value: u32) -> Self {
            let mut new_board = self.clone();
            new_board.set(row, col, value);
            new_board
        }

        pub fn with_value_at_index(&self, index: u32, value: u32) -> Self {
            let mut new_board = self.clone();
            new_board.set_with_index(index, value);
            new_board
        }

        pub fn is_empty_at(&self, row: u32, col: u32) -> bool {
            self.get(row, col) == 0
        }

        pub fn is_empty_at_index(&self, index: u32) -> bool {
            self.get_with_index(index) == 0
        }

        fn set(&mut self, row: u32, col: u32, value: u32) {
            self.content[row as usize][col as usize] = value;
        }

        fn set_with_index(&mut self, index: u32, value: u32) {
            let row = index / self.dimension;
            let col = index % self.dimension;
            self.set(row, col, value)
        }

        pub fn is_full(&self) -> bool {
            for row in &self.content {
                for value in row {
                    if *value == 0 {
                        return false;
                    }
                }
            }
            true
        }

        pub fn has_chain(&self, player: u32) -> bool {
            let mut chain = 0;

            // rows
            for row_index in 0..self.dimension {
                for col_index in 0..self.dimension {
                    if self.get(row_index, col_index) == player {
                        chain += 1;
                    } else {
                        chain = 0;
                    }
                    if chain >= self.dimension {
                        return true;
                    }
                }
            }
            // cols
            for col_index in 0..self.dimension {
                for row_index in 0..self.dimension {
                    if self.get(row_index, col_index) == player {
                        chain += 1;
                    } else {
                        chain = 0;
                    }
                    if chain >= self.dimension {
                        return true;
                    }
                }
            }
            // diagonals
            for offset in 0..self.dimension {
                if self.get(offset, offset) == player {
                    chain += 1;
                } else {
                    chain = 0;
                }
                if chain >= self.dimension {
                    return true;
                }
            }
            for offset in 0..self.dimension {
                if self.get(offset, self.dimension - offset - 1) == player {
                    chain += 1;
                } else {
                    chain = 0;
                }
                if chain >= self.dimension {
                    return true;
                }
            }
            false
        }

    }



    
}


#[cfg(test)]
mod tests {
}
