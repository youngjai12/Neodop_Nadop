#pragma once
#include "gridtypes.h"

/* Container for 'GPS' coordinate input (prototype!!), Contains double lat, double lon, uid_t uid.*/
class Coordinate {
public:
	/* lat: latitude of GPS data. lon: longitude of GPS data. */
	double lat, lon;
	uid_t uid;

	Coordinate(double latitude, double longitude, uid_t uid)
		: lat(latitude), lon(longitude), uid(uid) {};
};
