use crate::GameBoard;

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
}
