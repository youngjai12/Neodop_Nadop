#pragma once
#include <unordered_map>
#include "Coordinate.h"

/* Container for 'grid' coordinate!  Contains int x, int y. */
class GridCoord {
public:
	int x, y;

	GridCoord(int x, int y) : x(x), y(y) {}
	/* Construct by Coordinate. has transformation formula to translate GPS coordinate into Grid Coordinate. */
	GridCoord(const Coordinate &c) : x((c.lat + 180) * 1000000 / 3), y((c.lon + 180) * 1000000 / 3) {}

	~GridCoord() {};

	friend std::hash<GridCoord>;
	friend bool operator==(const GridCoord& c1, const GridCoord& c2);
	friend bool operator!=(const GridCoord& c1, const GridCoord& c2);
};

namespace std {
	template<>
	class hash<GridCoord> {
	public:
		size_t operator() (const GridCoord &crd) const {
			using std::hash;
			return hash<int>()(crd.x) ^ hash<int>()(crd.y);  // Our hash function for GridCoord. can be optimized?
		}
	};
};

inline bool operator==(const GridCoord& c1, const GridCoord& c2) {
	return (c1.x == c2.x) && (c1.y == c2.y);
};

inline bool operator!=(const GridCoord& c1, const GridCoord& c2) {
	return (c1.x != c2.x) || (c1.y != c2.y);
}