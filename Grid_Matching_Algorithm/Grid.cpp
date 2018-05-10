#include "Grid.h"


Grid::Grid() {
	grid_data.rehash(GRID_HASH_TABLE_RESERVE_BUCKET_SIZE);
	user_data.reserve(USER_HASH_TABLE_RESERVE_ELEM_SIZE);
}
Grid::~Grid() {};

auto Grid::_find_cell(uid_t uid) {
	return user_data.find(uid);
}

auto Grid::_search_grid(const GridCoord &c1) {
	return grid_data.equal_range(c1);
}


void Grid::insert_user(const GridCoord &c1, uid_t uid) {
	/* newly insert only if there is no user with uid in the grid. */
	if (_find_cell(uid) == user_data.end()) {
		grid_data.insert({ c1, uid });
		user_data.insert({ uid, c1 });
	}
	else {
		// TODO: If there is a user already in the grid, should it be updated? or ignored? or raise error?
	}
}

void Grid::delete_user(uid_t uid) {
	auto got = _find_cell(uid);

	/* delete only if there is a user with uid in the grid. */
	if (got != user_data.end())
	{
		auto cells = _search_grid(got->second);
		user_data.erase(got);

		for (auto cell = cells.first; cell != cells.second; cell++)
		{
			if (cell->second == uid)
			{
				grid_data.erase(cell);
				break;
			}
		}
	}
}

void Grid::update_user(uid_t uid, const GridCoord &new_c) {
	delete_user(uid);
	insert_user(new_c, uid);
}


GridCoord Grid::find_cell(uid_t uid) {
	auto got = _find_cell(uid);

	if (got == user_data.end()) {
		return CELL_NOT_FOUND;
	}
	else {
		return got->second;
	}
}



inline void Grid::insert_user(const Coordinate &c1) {
	insert_user(GridCoord(c1), c1.uid);
}

inline void Grid::delete_user(const Coordinate &c1) {
	delete_user(c1.uid);
}

inline void Grid::update_user(const Coordinate &c1) {
	update_user(c1.uid, GridCoord(c1));
}


int Grid::test_run() {
	# TODO: Write some test cases for the Grid!
	return 0;
}
