#pragma once
#include "GridCoord.h"


/* class Grid : for management of the grid. */
class Grid {
private:
	/* grid_t is the hash table for the grid system itself, using Coordinate as a key,
	*  and having uids as values. There can be multiple users in one cell, so use unorderd_multimap. */
	typedef std::unordered_multimap<GridCoord, uid_t> grid_t;
	/* usermap_t is the hash table for the user list, containing all users and its position. */
	typedef std::unordered_map<uid_t, GridCoord> usermap_t;

	/* representation of the grid. taking GridCoord as key, having uids as values. */
	grid_t grid_data;
	/* representation of the user list. taking uid (should be unique!) as key, having Gridcoord as value. */
	usermap_t user_data;

	/* finds given uid from the user_data, to determine it's in the grid and where it is. */
	auto _find_cell(uid_t uid);
	/* returns list of <key, value> of matching key, GridCoord c1. */
	auto _search_grid(const GridCoord &c1);

public:
	/* newly insert user to the grid. What if there is already user with uid in the grid? */
	void insert_user(const GridCoord &c1, uid_t uid);
	/* delete user with uid in the grid. does nothing if there was no user (maybe return GridCoord or deleted user?)*/
	void delete_user(uid_t uid);
	/* deletes user from grid and newly insert. maybe there is some efficient way? */
	void update_user(uid_t uid, const GridCoord &new_c);

	// below are same with above, just taking Coordinate as argument.
	void insert_user(const Coordinate &c1);
	void update_user(const Coordinate &c1);
	void delete_user(const Coordinate &c1);

	/* Finds which grid the user with uid is. If there's no user with uid, return CELL_NOT_FOUND = GridCoord(-1, -1). */
	GridCoord find_cell(uid_t uid);


	int test_run();

	Grid();
	~Grid();
};
