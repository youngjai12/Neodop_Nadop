#include <iostream>
#include <unordered_map>
#include <vector>

#define HASH_TABLE_RESERVE_SIZE 5000000


/* Type for uid : unsigned int. can maybe changed? */
typedef unsigned int uid_t;


/* Container for 'GPS' coordinate input (prototype), Contains double lat, double lon, uid_t uid.*/
class Coordinate {
public:
	double lat, lon;
	uid_t uid;

	Coordinate(double latitude, double longitude, int uid)
		: lat(latitude), lon(longitude), uid(uid) {}
};


/* Container for 'grid' coordinate!  Contains int x, int y. */
class GridCoord {
public:
	int x, y;

	GridCoord(int x, int y) : x(x), y(y) {}
	GridCoord(const Coordinate &c) : x((c.lat + 180) * 1000000 / 3), y((c.lon + 180) * 1000000 / 3) {}

	friend std::hash<GridCoord>;
	friend bool operator==(const GridCoord& c1, const GridCoord& c2);
};

namespace std {
	template<>
	class hash<GridCoord> {
	public:
		size_t operator() (const GridCoord &crd) const {
			using std::hash;
			return hash<int>()(crd.x) ^ hash<int>()(crd.y);
		}
	};
};

bool operator==(const GridCoord& c1, const GridCoord& c2) {
	return (c1.x == c2.x) && (c1.y == c2.y);
};


/* class Grid : for management of the grid. */
class Grid {
private:
	/* grid_t is the hash table for the grid, using STL unordered_map. maybe we should use unorderd_multimap! */
	typedef std::unordered_map<GridCoord, std::vector<uid_t> *> grid_t;

	grid_t data;
	
public:
	void insert_user(GridCoord c1, uid_t uid);
	std::vector<uid_t> *find_cell(GridCoord c1);
	void update_user(uid_t uid, GridCoord new_c);


	void insert_user(Coordinate c1);
	void update_user(Coordinate c1);


	GridCoord find_grid(uid_t uid);

	Grid() {
		data.reserve(HASH_TABLE_RESERVE_SIZE);
	}
	/* Releases all vector<> pointers of the grid.*/
	~Grid() {
		for (grid_t::iterator itor = data.begin(); itor != data.end(); itor++) {
			itor->second->~vector();
			free(itor->second);
		}
	}
};



int main() {
	Grid g1;
	
	g1.insert_user(GridCoord(1, 2), 1123);

	std::vector<uid_t> *test = g1.find_cell(GridCoord(1, 2));

	if (test != NULL) std::cout << (*test)[0] << std::endl;
	else std::cout << "fail" << std::endl;
	int i;
	std::cin >> i;
}


std::vector<uid_t> *Grid::find_cell(GridCoord c1) {
	grid_t::const_iterator got = data.find(c1);

	if (got == data.end())
		return NULL;
	else
		return got->second;
}

void Grid::insert_user(GridCoord c1, uid_t uid) {
	std::vector<uid_t> *cell = find_cell(c1);

	if (cell == NULL) {
		cell = new std::vector<uid_t>;
		data.insert({ c1, cell });
	}

	cell->push_back(uid);
}

void Grid::insert_user(Coordinate c1) {
	insert_user(GridCoord(c1), c1.uid);
}
