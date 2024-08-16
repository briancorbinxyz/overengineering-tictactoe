use std::{
    ffi::{c_char, c_int, CString},
    slice,
};

/// ----------------------------------------------------------------------------
/// # TicTacToe Library FFI bindings
/// ----------------------------------------------------------------------------

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
pub unsafe extern "C" fn version_string(callback: Callback) {
    let version = env!("CARGO_PKG_VERSION");
    let c_string = CString::new(version).expect("CString::new failed");
    callback(c_string.as_ptr(), (version.len() + 1) as c_int);
}

/// ----------------------------------------------------------------------------
/// # TicTacToe GameBoard FFI bindings
/// ----------------------------------------------------------------------------

#[no_mangle]
pub extern "C" fn new_game_board(dimension: u32) -> *mut tictactoe::GameBoard {
    Box::into_raw(Box::new(tictactoe::GameBoard::new(dimension)))
}

#[no_mangle]
pub unsafe extern "C" fn free_game_board(game_board: *mut tictactoe::GameBoard) {
    drop(Box::from_raw(game_board));
}

#[no_mangle]
pub unsafe extern "C" fn get_game_board_dimension(game_board: *mut tictactoe::GameBoard) -> u32 {
    (*game_board).get_dimension()
}

#[no_mangle]
pub unsafe extern "C" fn get_game_board_value_at_index(
    game_board: *mut tictactoe::GameBoard,
    index: u32,
) -> u32 {
    (*game_board).get_with_index(index)
}

#[no_mangle]
pub unsafe extern "C" fn get_game_board_with_value_at_index(
    game_board: *mut tictactoe::GameBoard,
    index: u32,
    value: u32,
) -> *mut tictactoe::GameBoard {
    Box::into_raw(Box::new((*game_board).with_value_at_index(index, value)))
}

#[no_mangle]
pub unsafe extern "C" fn get_game_board_is_full(game_board: *mut tictactoe::GameBoard) -> bool {
    (*game_board).is_full()
}

#[no_mangle]
pub unsafe extern "C" fn get_game_board_has_chain(
    game_board: *mut tictactoe::GameBoard,
    value: u32,
) -> bool {
    (*game_board).has_chain(value)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_ffi_can_manage_game_board_lifecycle_with_raw_pointer() {
        let board_ptr = new_game_board(3);
        assert!(!board_ptr.is_null());
        unsafe {
            free_game_board(board_ptr);
        }
    }

    #[test]
    fn test_ffi_can_get_game_board_value_at_index() {
        let board_ptr = new_game_board(3);
        assert!(!board_ptr.is_null());
        unsafe {
            assert_eq!(get_game_board_value_at_index(board_ptr, 0), 0);
            free_game_board(board_ptr);
        }
    }

    #[test]
    fn test_ffi_can_update_a_gameboard_immutably() {
        let board_ptr = new_game_board(3);
        unsafe {
            let updated_board_ptr = get_game_board_with_value_at_index(board_ptr, 4, 2);
            assert_eq!(get_game_board_value_at_index(board_ptr, 4), 0);
            assert_eq!(get_game_board_value_at_index(updated_board_ptr, 4), 2);
            free_game_board(board_ptr);
            free_game_board(updated_board_ptr);
        }
    }

    #[test]
    fn test_ffi_can_check_empty_game_board_has_available_moves() {
        let board_ptr = new_game_board(3);
        unsafe {
            assert!(!get_game_board_is_full(board_ptr));
            free_game_board(board_ptr);
        }
    }

    #[test]
    fn test_ffi_can_check_full_game_board_has_no_available_moves() {
        let mut board_ptr = new_game_board(3);
        unsafe {
            for i in 0..9 {
                board_ptr = get_game_board_with_value_at_index(board_ptr, i, i % 2 + 1);
            }
            assert!(get_game_board_is_full(board_ptr));
        }
    }

    #[test]
    fn test_ffi_can_check_for_winning_chain() {
        let mut board_ptr = new_game_board(3);
        unsafe {
            for i in 0..3 {
                board_ptr = get_game_board_with_value_at_index(board_ptr, i, 1);
            }
            assert!(get_game_board_has_chain(board_ptr, 1));
            free_game_board(board_ptr);
        }
    }
}

/// ----------------------------------------------------------------------------
/// # TicTacToe
/// ----------------------------------------------------------------------------

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

        pub fn get_dimension(&self) -> u32 {
            self.dimension
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
            // rows
            let mut chain: u32;
            for row_index in 0..self.dimension {
                chain = 0;
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
                chain = 0;
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
            chain = 0;
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
            chain = 0;
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

    #[cfg(test)]
    mod tests {
        use super::*;

        #[test]
        fn test_new_game_board_creation() {
            let board = GameBoard::new(3);
            assert_eq!(board.dimension, 3);
            assert_eq!(board.content.len(), 3);
            for row in board.content {
                assert_eq!(row.len(), 3);
                for cell in row {
                    assert_eq!(cell, 0);
                }
            }
        }

        #[test]
        fn test_new_game_board_empty() {
            let board = GameBoard::new(0);
            assert_eq!(board.dimension, 0);
            assert!(board.content.is_empty());
        }

        #[test]
        fn test_new_game_board_large() {
            let dimension = 100;
            let board = GameBoard::new(dimension);
            assert_eq!(board.dimension, dimension);
            assert_eq!(board.content.len(), dimension as usize);
            for row in board.content {
                assert_eq!(row.len(), dimension as usize);
                for cell in row {
                    assert_eq!(cell, 0);
                }
            }
        }

        #[test]
        fn test_set_and_retrieve_with_index() {
            let mut board = GameBoard::new(3);
            board.set_with_index(0, 99);
            assert_eq!(board.get_with_index(0), 99);
        }

        #[test]
        fn test_immutably_set_with_index() {
            let board = GameBoard::new(3);
            let new_board = board.with_value_at_index(0, 99);
            assert_eq!(board.get_with_index(0), 0);
            assert_eq!(new_board.get_with_index(0), 99);
        }

        #[test]
        fn test_can_check_for_empty() {
            let board = GameBoard::new(1);
            assert_eq!(board.is_empty_at(0, 0), true);
            assert_eq!(board.is_empty_at_index(0), true);
        }

        #[test]
        fn test_can_check_for_full() {
            let mut board = GameBoard::new(1);
            board.set(0, 0, 1);
            assert_eq!(board.is_full(), true);
        }

        #[test]
        fn test_empty_board_has_no_chain() {
            let board = GameBoard::new(3);
            assert_eq!(board.has_chain(1), false);
        }

        #[test]
        fn test_can_detect_winning_row_chain() {
            let mut board = GameBoard::new(3);
            for r in 0..3 {
                board.set(r, 0, 1);
            }
            assert_eq!(board.has_chain(1), true);
        }

        #[test]
        fn test_can_detect_winning_column_chain() {
            let mut board = GameBoard::new(3);
            for c in 0..3 {
                board.set(1, c, 1);
            }
            assert_eq!(board.has_chain(1), true);
        }

        #[test]
        fn test_can_detect_diagonal_winning_column_chain() {
            let mut board = GameBoard::new(3);
            for o in 0..3 {
                board.set(o, o, 1);
            }
            assert_eq!(board.has_chain(1), true);
        }
    }
}