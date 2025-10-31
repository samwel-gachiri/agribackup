# API Documentation for Admin and Exporter Services

This document provides detailed specifications for the REST API endpoints in the `AdminController` and `ExporterController` of the Farmers Portal API. These endpoints support operations for managing System Admins, Zone Supervisors, zones, farmers, and pickup schedules. The APIs are secured with JWT-based authentication (`bearerAuth`) and use role-based access control to enforce permissions.

## Base Information
- **Base URL**: `/admin-service` (for `AdminController`) and `/exporters-service/exporter` (for `ExporterController`)
- **Authentication**: All endpoints require a valid JWT token in the `Authorization` header (`Bearer <token>`). The token must include the user’s ID (`username`) and role (`ROLE_SYSTEM_ADMIN`, `ROLE_ZONE_SUPERVISOR`, or `ROLE_EXPORTER`).
- **Response Format**: All endpoints return a `Result` wrapper with the following structure:
  - Success: `{ "success": true, "data": <T>, "message": <string> }`
  - Failure: `{ "success": false, "message": <string> }`
- **Content Type**: `application/json` for requests and responses.
- **Error Codes**:
  - `200`: Success
  - `400`: Invalid input (e.g., missing required fields)
  - `403`: Forbidden (user lacks required permissions or is not authorized)
  - `404`: Resource not found (e.g., zone, farmer, or user)
  - `409`: Conflict (e.g., duplicate email or phone number)

## AdminController Endpoints (/admin-service)

### 1. Create a New Zone
- **Endpoint**: `POST /admin-service/zones`
- **Description**: Creates a new operational zone for an Exporter, with the authenticated user (System Admin or Exporter) set as the creator.
- **Permissions**: Requires `CREATE_ZONE` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Body**: `CreateZoneRequestDto`
    ```json
    {
      "name": "string",
      "produceType": "string" | null,
      "centerLatitude": number,
      "centerLongitude": number,
      "radiusKm": number,
      "exporterId": "string"
    }
    ```
    - `name`: Required, name of the zone (e.g., "North Region").
    - `produceType`: Optional, type of produce (e.g., "Maize").
    - `centerLatitude`: Required, latitude of zone center.
    - `centerLongitude`: Required, longitude of zone center.
    - `radiusKm`: Required, radius in kilometers.
    - `exporterId`: Required, UUID of the Exporter owning the zone.
- **Response**:
  - **200**: `Result<ZoneResponseDto>`
    ```json
    {
      "success": true,
      "data": {
        "id": "string",
        "name": "string",
        "produceType": "string" | null,
        "centerLatitude": number,
        "centerLongitude": number,
        "radiusKm": number,
        "exporterId": "string",
        "creatorId": "string",
        "comments": "string" | null,
        "farmerCount": number,
        "supervisorIds": ["string"]
      },
      "message": "Zone created successfully"
    }
    ```
  - **400**: Invalid input (e.g., missing required fields).
  - **404**: Exporter or creator user not found.
- **Notes**:
  - The `creatorId` in the response is set to the authenticated user’s ID from the JWT (`userDetails.username`).
  - Used by System Admins or Exporters to define operational zones.

### 2. Assign Zone Supervisor to Zone
- **Endpoint**: `POST /admin-service/zones/{zoneId}/supervisors`
- **Description**: Assigns a Zone Supervisor to a specific zone.
- **Permissions**: Requires `ADD_ZONE_SUPERVISOR` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `zoneId` (UUID of the zone)
  - **Body**: `AssignZoneSupervisorDto`
    ```json
    {
      "zoneSupervisorId": "string"
    }
    ```
    - `zoneSupervisorId`: Required, UUID of the Zone Supervisor.
- **Response**:
  - **200**: `Result<ZoneResponseDto>` (same structure as above)
  - **400**: Zone Supervisor is not active.
  - **404**: Zone or Zone Supervisor not found.
- **Notes**:
  - The Zone Supervisor must have `status = "ACTIVE"`.
  - If the Zone Supervisor is already assigned to the zone, the operation is idempotent (no changes made).

### 3. Add Farmer to Zone
- **Endpoint**: `POST /admin-service/zones/{zoneId}/farmers`
- **Description**: Adds a Farmer to a zone, creating a `FarmerExporterRelationship`.
- **Permissions**: Requires `ADD_FARMER` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `zoneId` (UUID of the zone)
  - **Body**: `AddFarmerToZoneDto`
    ```json
    {
      "farmerId": "string"
    }
    ```
    - `farmerId`: Required, UUID of the Farmer.
- **Response**:
  - **200**: `Result<FarmerInZoneResponseDto>`
    ```json
    {
      "success": true,
      "data": {
        "farmerId": "string",
        "farmerName": "string",
        "farmSize": number | null,
        "farmName": "string",
        "location": "string" | null,
        "joinedAt": "string" // ISO 8601 datetime
      },
      "message": "Farmer added to zone successfully"
    }
    ```
  - **400**: Farmer already in zone.
  - **404**: Zone or Farmer not found.
- **Notes**:
  - Used by System Admins or Zone Supervisors to assign Farmers to zones.
  - Checks for duplicate relationships to prevent adding the same Farmer multiple times.

### 4. Get Zone Supervisor Details
- **Endpoint**: `GET /admin-service/zone-supervisors/{zoneSupervisorId}`
- **Description**: Retrieves details of a Zone Supervisor, including their assigned zones.
- **Permissions**: Requires `VIEW_ZONE_SUPERVISOR` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `zoneSupervisorId` (UUID of the Zone Supervisor)
- **Response**:
  - **200**: `Result<ZoneSupervisorResponseDto>`
    ```json
    {
      "success": true,
      "data": {
        "id": "string",
        "userId": "string",
        "fullName": "string",
        "email": "string" | null,
        "phoneNumber": "string" | null,
        "status": "string",
        "zones": [{ /* ZoneResponseDto structure */ }],
        "createdAt": "string",
        "updatedAt": "string"
      },
      "message": "Zone Supervisor retrieved successfully"
    }
    ```
  - **404**: Zone Supervisor not found.
- **Notes**:
  - Used by Exporters or System Admins to view Zone Supervisor details, including their assigned zones.

### 5. Get Zone Details
- **Endpoint**: `GET /admin-service/zones/{zoneId}`
- **Description**: Retrieves details of a zone, including comments, for authorized users (Exporters, System Admins, or Farmers in the zone).
- **Permissions**: Requires `VIEW_ZONE_SUPERVISOR` or `ADD_FARMER` authority. Farmers must be associated with the zone.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `zoneId` (UUID of the zone)
- **Response**:
  - **200**: `Result<ZoneResponseDto>` (same structure as above)
  - **403**: Farmer not associated with the zone.
  - **404**: Zone or user not found.
- **Notes**:
  - Farmers can only view zones they are associated with (checked via `FarmerExporterRelationship`).
  - Includes `comments` field for visibility to authorized users.

### 6. Add or Update Zone Comment
- **Endpoint**: `PUT /admin-service/zones/{zoneId}/comments`
- **Description**: Allows a Zone Supervisor to add or update a comment on a zone they are assigned to.
- **Permissions**: Requires `ADD_ZONE_COMMENT` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `zoneId` (UUID of the zone)
  - **Body**: `UpdateZoneCommentDto`
    ```json
    {
      "comments": "string" | null
    }
    ```
    - `comments`: Optional, comment text (e.g., "Heavily infested").
- **Response**:
  - **200**: `Result<ZoneResponseDto>` (same structure as above)
  - **403**: Zone Supervisor not assigned to the zone.
  - **404**: Zone or Zone Supervisor not found.
- **Notes**:
  - Only Zone Supervisors assigned to the zone can update comments.
  - Setting `comments` to `null` clears the existing comment.

### 7. Edit Farmer Details
- **Endpoint**: `PUT /admin-service/farmers/{farmerId}`
- **Description**: Allows a Zone Supervisor to edit a Farmer’s details with consent.
- **Permissions**: Requires `EDIT_FARMER` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `farmerId` (UUID of the Farmer)
  - **Body**: `UpdateFarmerRequestDto`
    ```json
    {
      "farmName": "string" | null,
      "farmSize": number | null,
      "location": "string" | null,
      "consentToken": "string" | null
    }
    ```
    - `farmName`, `farmSize`, `location`: Optional fields to update.
    - `consentToken`: Required for consent verification (implementation-specific).
- **Response**:
  - **200**: `Result<FarmerResponseDto>`
    ```json
    {
      "success": true,
      "data": {
        "id": "string",
        "farmName": "string",
        "farmSize": number | null,
        "location": "string" | null,
        "userId": "string",
        "fullName": "string",
        "email": "string" | null,
        "phoneNumber": "string" | null
      },
      "message": "Farmer details updated successfully"
    }
    ```
  - **403**: Zone Supervisor not authorized or consent not provided.
  - **404**: Farmer or Zone Supervisor not found.
- **Notes**:
  - The Zone Supervisor must manage a zone containing the Farmer.
  - Consent verification is a placeholder; implement based on your mechanism (e.g., token stored in `Farmer`).

### 8. Schedule a Pickup
- **Endpoint**: `POST /admin-service/pickup-schedules`
- **Description**: Allows a Zone Supervisor to schedule a pickup for a Farmer in their zone.
- **Permissions**: Requires `SCHEDULE_PICKUP` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Body**: `SchedulePickupRequestDto`
    ```json
    {
      "farmerId": "string",
      "exporterId": "string",
      "produceListingId": "string",
      "scheduledDate": "string", // ISO 8601 datetime
      "pickupNotes": "string" | null
    }
    ```
    - `farmerId`, `exporterId`, `produceListingId`: Required UUIDs.
    - `scheduledDate`: Required pickup date/time.
    - `pickupNotes`: Optional notes for the pickup.
- **Response**:
  - **200**: `Result<PickupScheduleResponseDto>`
    ```json
    {
      "success": true,
      "data": {
        "id": "string",
        "exporterId": "string",
        "farmerId": "string",
        "produceListingId": "string",
        "scheduledDate": "string",
        "status": "string",
        "pickupNotes": "string" | null,
        "createdAt": "string",
        "updatedAt": "string"
      },
      "message": "Pickup scheduled successfully"
    }
    ```
  - **400**: Invalid input.
  - **403**: Zone Supervisor not authorized for the Farmer/Exporter.
  - **404**: Farmer, Exporter, or produce listing not found.
- **Notes**:
  - The Zone Supervisor must manage a zone with the specified Farmer and Exporter.

## ExporterController Endpoints (/exporters-service/exporter)

### 1. Get Exporter Details
- **Endpoint**: `GET /exporters-service/exporter/{exporterId}`
- **Description**: Retrieves detailed information about an Exporter by their ID.
- **Permissions**: Requires Exporter role or appropriate authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `exporterId` (UUID of the Exporter)
- **Response**:
  - **200**: `Result<ExporterResponseDto>`
    ```json
    {
      "success": true,
      "data": {
        "id": "string",
        "companyName": "string",
        "companyDesc": "string",
        "licenseId": "string",
        "verificationStatus": "string"
      },
      "message": "Success"
    }
    ```
  - **404**: Exporter not found.
- **Notes**: Unchanged from previous controller.

### 2. Update Exporter Profile
- **Endpoint**: `PUT /exporters-service/exporter/{exporterId}`
- **Description**: Updates an existing Exporter’s information.
- **Permissions**: Requires Exporter role or appropriate authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `exporterId` (UUID of the Exporter)
  - **Body**: `UpdateExporterRequestDto`
    ```json
    {
      "name": "string" | null,
      "email": "string" | null,
      "phoneNumber": "string" | null
    }
    ```
- **Response**:
  - **200**: `Result<ExporterResponseDto>` (same structure as above)
  - **400**: Invalid input.
  - **404**: Exporter not found.
- **Notes**: Unchanged from previous controller.

### 3. Verify Exporter
- **Endpoint**: `PUT /exporters-service/exporter/{exporterId}/verify`
- **Description**: Changes an Exporter’s verification status to `VERIFIED`.
- **Permissions**: Requires appropriate authority (e.g., admin-level access).
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `exporterId` (UUID of the Exporter)
- **Response**:
  - **200**: `Result<ExporterResponseDto>` (same structure as above)
  - **404**: Exporter not found.
- **Notes**: Unchanged from previous controller.

### 4. Create a New Zone
- **Endpoint**: `POST /exporters-service/exporter/zones`
- **Description**: Creates a new operational zone for an Exporter.
- **Permissions**: Requires Exporter role or appropriate authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Body**: `CreateZoneRequestDto` (same structure as in `AdminController`)
- **Response**:
  - **200**: `Result<ZoneResponseDto>` (same structure as above)
  - **400**: Invalid input.
  - **404**: Exporter not found.
- **Notes**: Unchanged from previous controller, but note that `AdminController` provides a similar endpoint with creator tracking.

### 5. Add Farmer to Zone
- **Endpoint**: `POST /exporters-service/exporter/zones/{zoneId}/farmers/{farmerId}`
- **Description**: Establishes a relationship between a Farmer and an Exporter’s zone.
- **Permissions**: Requires Exporter role or appropriate authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameters**: `zoneId` (UUID of the zone), `farmerId` (UUID of the Farmer)
- **Response**:
  - **200**: `Result<FarmerInZoneResponseDto>` (same structure as above)
  - **404**: Zone or Farmer not found.
- **Notes**: Unchanged from previous controller, but `AdminController` provides a similar endpoint with a request body (`AddFarmerToZoneDto`).

### 6. Get Exporter’s Zones
- **Endpoint**: `GET /exporters-service/exporter/{exporterId}/zones`
- **Description**: Retrieves all zones associated with an Exporter.
- **Permissions**: Requires Exporter role or appropriate authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `exporterId` (UUID of the Exporter)
- **Response**:
  - **200**: `Result<List<ZoneResponseDto>>` (list of ZoneResponseDto structures)
  - **404**: Exporter not found.
- **Notes**: Unchanged from previous controller, but `ZoneResponseDto` now includes `creatorId`, `comments`, and `supervisorIds`.

### 7. Get Farmers in Zone
- **Endpoint**: `GET /exporters-service/exporter/zones/{zoneId}/farmers`
- **Description**: Retrieves all Farmers associated with a specific zone.
- **Permissions**: Requires Exporter role or appropriate authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `zoneId` (UUID of the zone)
- **Response**:
  - **200**: `Result<List<FarmerInZoneResponseDto>>` (list of FarmerInZoneResponseDto structures)
  - **404**: Zone not found.
- **Notes**: Unchanged from previous controller.

### 8. Get Paginated Farmers in Zone
- **Endpoint**: `GET /exporters-service/exporter/zones/{zoneId}/farmers/paginated`
- **Description**: Retrieves a paginated list of Farmers associated with a specific zone.
- **Permissions**: Requires Exporter role or appropriate authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `zoneId` (UUID of the zone)
  - **Query Parameters**: `page`, `size`, `sort` (standard Spring Data pagination parameters)
- **Response**:
  - **200**: `Result<Page<FarmerInZoneResponseDto>>`
    ```json
    {
      "success": true,
      "data": {
        "content": [{ /* FarmerInZoneResponseDto structure */ }],
        "pageable": { /* Pagination metadata */ },
        "totalPages": number,
        "totalElements": number,
        "size": number,
        "number": number
      },
      "message": "Zone farmers retrieved successfully"
    }
    ```
  - **404**: Zone not found.
- **Notes**: Unchanged from previous controller.

### 9. Schedule a Pickup
- **Endpoint**: `POST /exporters-service/exporter/pickups`
- **Description**: Creates a new pickup schedule for produce collection.
- **Permissions**: Requires Exporter role or appropriate authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Body**: `SchedulePickupRequestDto` (same structure as in `AdminController`)
- **Response**:
  - **200**: `Result<PickupScheduleResponseDto>` (same structure as above)
  - **400**: Invalid input.
  - **404**: Farmer or Exporter not found.
- **Notes**: Unchanged from previous controller, but `AdminController` provides a similar endpoint for Zone Supervisors with additional authorization checks.

### 10. Get Pickup Schedules
- **Endpoint**: `GET /exporters-service/exporter/{exporterId}/pickups`
- **Description**: Retrieves a paginated list of pickup schedules for an Exporter.
- **Permissions**: Requires Exporter role or appropriate authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `exporterId` (UUID of the Exporter)
  - **Query Parameters**: `page`, `size`, `sort`
- **Response**:
  - **200**: `Result<Page<PickupScheduleResponseDto>>` (similar to paginated farmers response)
  - **404**: Exporter not found.
- **Notes**: Unchanged from previous controller.

### 11. Update Zone Details
- **Endpoint**: `PUT /exporters-service/exporter/zones/{zoneId}`
- **Description**: Updates an existing zone’s information.
- **Permissions**: Requires Exporter role or appropriate authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `zoneId` (UUID of the zone)
  - **Body**: `UpdateZoneRequestDto`
    ```json
    {
      "name": "string",
      "produceType": "string" | null,
      "centerLatitude": number,
      "centerLongitude": number,
      "radiusKm": number
    }
    ```
- **Response**:
  - **200**: `Result<ZoneResponseDto>` (same structure as above)
  - **400**: Invalid input.
  - **404**: Zone not found.
- **Notes**: Unchanged from previous controller.

### 12. Add a New System Admin
- **Endpoint**: `POST /exporters-service/exporter/system-admins`
- **Description**: Creates a new System Admin account with the `SYSTEM_ADMIN` role.
- **Permissions**: Requires `MANAGE_SYSTEM_ADMIN` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Body**: `SystemAdminRegistrationDto`
    ```json
    {
      "user": {
        "email": "string" | null,
        "password": "string",
        "fullName": "string",
        "phoneNumber": "string" | null
      }
    }
    ```
- **Response**:
  - **200**: `Result<SystemAdminResponseDto>`
    ```json
    {
      "success": true,
      "data": {
        "id": "string",
        "userId": "string",
        "fullName": "string",
        "email": "string" | null,
        "phoneNumber": "string" | null,
        "status": "string",
        "createdAt": "string",
        "updatedAt": "string"
      },
      "message": "System Admin created successfully"
    }
    ```
  - **400**: Invalid input.
  - **409**: Email or phone number already registered as System Admin.
- **Notes**: Used by Exporters to manage System Admins.

### 13. Remove a System Admin
- **Endpoint**: `DELETE /exporters-service/exporter/system-admins/{systemAdminId}`
- **Description**: Marks a System Admin as `DELETED` (soft delete).
- **Permissions**: Requires `MANAGE_SYSTEM_ADMIN` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `systemAdminId` (UUID of the System Admin)
- **Response**:
  - **200**: `Result<String>`
    ```json
    {
      "success": true,
      "data": "deleted",
      "message": "System Admin deleted successfully"
    }
    ```
  - **400**: System Admin already deleted.
  - **404**: System Admin not found.
- **Notes**: Soft deletion ensures auditability.

### 14. Add a New Zone Supervisor
- **Endpoint**: `POST /exporters-service/exporter/zone-supervisors`
- **Description**: Creates a new Zone Supervisor account with the `ZONE_SUPERVISOR` role.
- **Permissions**: Requires `MANAGE_ZONE_SUPERVISOR` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Body**: `ZoneSupervisorRegistrationDto`
    ```json
    {
      "user": {
        "email": "string" | null,
        "password": "string",
        "fullName": "string",
        "phoneNumber": "string" | null
      }
    }
    ```
- **Response**:
  - **200**: `Result<ZoneSupervisorResponseDto>` (same structure as in `AdminController`)
  - **400**: Invalid input.
  - **409**: Email or phone number already registered as Zone Supervisor.
- **Notes**: Used by Exporters to manage Zone Supervisors.

### 15. Remove a Zone Supervisor
- **Endpoint**: `DELETE /exporters-service/exporter/zone-supervisors/{zoneSupervisorId}`
- **Description**: Marks a Zone Supervisor as `DELETED` and clears their zone assignments.
- **Permissions**: Requires `MANAGE_ZONE_SUPERVISOR` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `zoneSupervisorId` (UUID of the Zone Supervisor)
- **Response**:
  - **200**: `Result<String>` (same structure as System Admin deletion)
  - **400**: Zone Supervisor already deleted.
  - **404**: Zone Supervisor not found.
- **Notes**: Clears zone assignments to prevent orphaned relationships.

### 16. Get All System Admins and Zone Supervisors
- **Endpoint**: `GET /exporters-service/exporter/users`
- **Description**: Retrieves a list of active System Admins and/or Zone Supervisors, optionally filtered by role.
- **Permissions**: Requires `MANAGE_SYSTEM_ADMIN` or `MANAGE_ZONE_SUPERVISOR` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Query Parameter**: `role` (optional, `SYSTEM_ADMIN` or `ZONE_SUPERVISOR`)
- **Response**:
  - **200**: `Result<List<Any>>`
    ```json
    {
      "success": true,
      "data": [
        { /* SystemAdminResponseDto or ZoneSupervisorResponseDto */ }
      ],
      "message": "Admins and Supervisors retrieved successfully"
    }
    ```
  - **400**: Invalid input (e.g., invalid role).
- **Notes**:
  - The response contains a mixed list of `SystemAdminResponseDto` and `ZoneSupervisorResponseDto`.
  - Use the presence of `zones` field to distinguish `ZoneSupervisorResponseDto`.

### 17. Add or Update Zone Comment
- **Endpoint**: `PUT /exporters-service/exporter/zones/{zoneId}/comments`
- **Description**: Allows an Exporter to add or update a comment on a zone.
- **Permissions**: Requires `ADD_ZONE_COMMENT` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `zoneId` (UUID of the zone)
  - **Body**: `UpdateZoneCommentDto` (same structure as in `AdminController`)
- **Response**:
  - **200**: `Result<ZoneResponseDto>` (same structure as above)
  - **403**: Exporter not authorized for the zone.
  - **404**: Zone not found.
- **Notes**:
  - Exporters can only comment on zones they own (checked via `exporterId`).

### 18. Edit Farmer Details
- **Endpoint**: `PUT /exporters-service/exporter/farmers/{farmerId}`
- **Description**: Allows an Exporter to edit a Farmer’s details with consent.
- **Permissions**: Requires `EDIT_FARMER` authority.
- **Request**:
  - **Headers**: `Authorization: Bearer <token>`
  - **Path Parameter**: `farmerId` (UUID of the Farmer)
  - **Body**: `UpdateFarmerRequestDto` (same structure as in `AdminController`)
- **Response**:
  - **200**: `Result<FarmerResponseDto>` (same structure as above)
  - **403**: Exporter not authorized or consent not provided.
  - **404**: Farmer not found.
- **Notes**:
  - Exporters must have a `FarmerExporterRelationship` with the Farmer.

## Frontend Integration Notes

To integrate these endpoints into your frontend (e.g., Vuex store with Axios), consider the following:

### 1. API Client Updates
- **Base URLs**:
  - Add support for `/admin-service` and `/exporters-service/exporter` in your API client.
  - Example (Axios in Vuex):
    ```javascript
    const apiClient = axios.create({
      baseURL: 'https://your-api-base-url',
      headers: { 'Content-Type': 'application/json' }
    });

    // Admin service endpoints
    export const createZone = (data, token) =>
      apiClient.post('/admin-service/zones', data, { headers: { Authorization: `Bearer ${token}` } });
    export const addZoneSupervisor = (zoneId, data, token) =>
      apiClient.post(`/admin-service/zones/${zoneId}/supervisors`, data, { headers: { Authorization: `Bearer ${token}` } });
    // ... other admin-service endpoints

    // Exporter service endpoints
    export const addSystemAdmin = (data, token) =>
      apiClient.post('/exporters-service/exporter/system-admins', data, { headers: { Authorization: `Bearer ${token}` } });
    export const getAdminsAndSupervisors = (role, token) =>
      apiClient.get('/exporters-service/exporter/users', { params: { role }, headers: { Authorization: `Bearer ${token}` } });
    // ... other exporter-service endpoints
    ```

### 2. JWT Handling
- Ensure the JWT token includes:
  - `sub` (user ID, used as `userDetails.username`).
  - `roles` (e.g., `ROLE_SYSTEM_ADMIN`, `ROLE_ZONE_SUPERVISOR`, `ROLE_EXPORTER`).
- Update the frontend to parse the JWT and route users to appropriate dashboards:
  - Exporter Dashboard: For managing System Admins, Zone Supervisors, zones, and farmers.
  - System Admin Dashboard: For creating zones and managing Zone Supervisors/farmers.
  - Zone Supervisor Dashboard: For editing farmers, adding zone comments, and scheduling pickups.
- Example (Vuex store):
  ```javascript
  const store = new Vuex.Store({
    state: {
      user: null,
      role: null
    },
    mutations: {
      setUser(state, { user, role }) {
        state.user = user;
        state.role = role;
      }
    },
    actions: {
      async login({ commit }, token) {
        const decoded = jwtDecode(token); // Use a JWT decoding library
        commit('setUser', { user: decoded.sub, role: decoded.roles[0]?.replace('ROLE_', '') });
      }
    }
  });
  ```

### 3. Response Handling
- Handle the `Result` wrapper consistently:
  - Check `success` field to determine if the request was successful.
  - Extract `data` for successful responses or `message` for errors.
  - Example:
    ```javascript
    async function handleApiCall(apiCall) {
      try {
        const response = await apiCall;
        if (response.data.success) {
          return response.data.data;
        } else {
          throw new Error(response.data.message);
        }
      } catch (error) {
        throw error;
      }
    }
    ```
- For `GET /exporters-service/exporter/users`, handle the mixed `List<Any>` response:
  ```javascript
  const users = await getAdminsAndSupervisors(role, token);
  users.forEach(user => {
    if (user.zones) { // ZoneSupervisorResponseDto
    } else { // SystemAdminResponseDto
    }
  });
  ```

### 4. Consent Mechanism
- Implement a consent flow for `PUT /admin-service/farmers/{farmerId}` and `PUT /exporters-service/exporter/farmers/{farmerId}`:
  - Add a UI component for Farmers to generate a `consentToken` (e.g., a button to confirm consent).
  - Store the token in the backend (e.g., in `Farmer` entity or a separate consent table).
  - Send the `consentToken` in `UpdateFarmerRequestDto`.
- Example (Vue component):
  ```vue
  <template>
    <button @click="generateConsentToken">Provide Consent</button>
    <form @submit.prevent="updateFarmer">
      <input v-model="form.farmName" placeholder="Farm Name" />
      <input v-model="form.consentToken" placeholder="Consent Token" />
      <button type="submit">Update Farmer</button>
    </form>
  </template>
  <script>
  export default {
    data() {
      return {
        form: { farmName: '', consentToken: '' }
      };
    },
    methods: {
      async generateConsentToken() {
        const response = await apiClient.post('/farmers-service/consent', {}, {
          headers: { Authorization: `Bearer ${this.$store.state.token}` }
        });
        this.form.consentToken = response.data.data.token;
      },
      async updateFarmer() {
        await handleApiCall(apiClient.put(`/admin-service/farmers/${farmerId}`, this.form, {
          headers: { Authorization: `Bearer ${this.$store.state.token}` }
        }));
      }
    }
  };
  </script>
  ```

### 5. Dashboard Updates
- **Exporter Dashboard**:
  - Add sections to manage System Admins and Zone Supervisors (`POST/DELETE /system-admins`, `POST/DELETE /zone-supervisors`).
  - Display a list of active admins/supervisors (`GET /users`) with filtering by role.
  - Allow commenting on zones (`PUT /zones/{zoneId}/comments`) and editing Farmer details (`PUT /farmers/{farmerId}`).
- **System Admin Dashboard**:
  - Support zone creation (`POST /admin-service/zones`) and Zone Supervisor/farmer management (`POST /admin-service/zones/{zoneId}/supervisors`, `POST /admin-service/zones/{zoneId}/farmers`).
- **Zone Supervisor Dashboard**:
  - Support editing Farmer details, adding zone comments, and scheduling pickups (`PUT /admin-service/farmers/{farmerId}`, `PUT /admin-service/zones/{zoneId}/comments`, `POST /admin-service/pickup-schedules`).
- Example (Vuex state for dashboard):
  ```javascript
  state: {
    zones: [],
    admins: [],
    supervisors: [],
    farmers: []
  },
  actions: {
    async fetchAdminsAndSupervisors({ commit }, role) {
      const users = await handleApiCall(getAdminsAndSupervisors(role, this.state.token));
      commit('setUsers', { admins: users.filter(u => !u.zones), supervisors: users.filter(u => u.zones) });
    }
  }
  ```

### 6. Error Handling
- Display error messages from `Result.message` in the UI (e.g., using toast notifications).
- Handle specific HTTP status codes (e.g., `403` for unauthorized, `404` for not found) to guide users appropriately.
- Example:
  ```javascript
  try {
    await handleApiCall(createZone(data, token));
  } catch (error) {
    if (error.response?.status === 403) {
      alert('You are not authorized to perform this action.');
    } else {
      alert(error.message);
    }
  }
  ```

## Notes
- **Consistency**: The endpoints maintain RESTful conventions and use the same `Result` wrapper as existing APIs, ensuring seamless integration with your frontend.
- **Security**: All endpoints require JWT authentication with role-based authorization (`@PreAuthorize` in services).
- **Consent Mechanism**: The `consentToken` in `UpdateFarmerRequestDto` is a placeholder. Implement a backend endpoint (e.g., `POST /farmers-service/consent`) to generate/verify tokens.
- **Pagination**: Endpoints like `GET /exporters-service/exporter/zones/{zoneId}/farmers/paginated` and `GET /exporters-service/exporter/{exporterId}/pickups` support pagination for scalability.
- **Swagger**: The Swagger documentation is embedded in the controllers using `@Operation` and `@ApiResponses`, accessible via your Swagger UI (e.g., `/swagger-ui.html`).

## Next Steps
- **Consent Implementation**: Define the consent mechanism (e.g., a `consentToken` field in `Farmer` or a separate endpoint) to finalize `editFarmerDetails`.
- **Frontend Code**: Provide specific Vuex/Axios code snippets or Vue components if needed to integrate these endpoints.
- **Testing**: Request unit/integration tests for the controllers to ensure reliability.
- **Additional Endpoints**: Specify any additional endpoints (e.g., bulk operations, detailed farmer reports) if required.

Please confirm if this documentation meets your needs or provide any specific adjustments (e.g., additional details, specific frontend examples) before proceeding!