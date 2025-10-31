<!DOCTYPE html>
<html lang="en">

<head>
    <title>Listing Report</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            background-color: #f9f9f9;
            margin: 0;
            padding: 0;
        }

        .header {
            background-color: #167038;
            color: white;
            padding: 10px;
            text-align: center;
        }

        .content {
            margin: 20px;
            background-color: white;
            padding: 20px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }

        .logo {
            display: inline-block;
            vertical-align: middle;
            margin-right: 10px;
        }

        .company-info {
            display: inline-block;
            vertical-align: middle;
        }

        .footer {
            background-color: #f9f9f9;
            padding: 10px;
            text-align: center;
            position: fixed;
            bottom: 0;
            width: 100%;
        }

        .container {
            display: flex;
            justify-content: space-between;
            flex-direction: row;
            align-items: center;
            margin-top: 10px;
        }

        .left-bar,
        .right-bar {
            height: 10px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }

        th, td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
        }

        th {
            background-color: #167038;
            color: white;
        }

        tr:nth-child(even) {
            background-color: #f2f2f2;
        }
    </style>
</head>

<body>

<div class="header">
    <img class="logo" src="https://agri-image.s3.us-east-1.amazonaws.com/uploads/logo.png" alt="Company logo" width="50" height="50"/>
    <span style="font-size: 24px; font-weight: bold;">Agriconnect KE</span>
</div>

<div class="content">
    <div class="company-info">
        <p style="font-size: 16px; font-weight: bold;">AGRICONNECT</p>
        <p>P.O Box 00100 Nairobi, Kenya,<br/> Tel: +254 (0)705 644 805,<br/> Email: samgachiri2002@gmail.com</p>
    </div>

    <h2 style="color: #13361C; font-weight: bold;">${listingTitle}</h2>

    <hr style="background-color: #f2f2f2;"/>
    <img class="logo" src="${produceImage}" alt="Company logo" width="50" height="50"/>
    <h2>Produce: ${produceName}</h2>
    <h3>Quantity Sold: ${quantitySold}</h3>
    <h3>Total Earnings: <span style="color: green;"> ${currency} ${earnings}</span></h3>

    <table>
        <thead>
        <tr>
            <th>Date</th>
            <th>Produce</th>
            <th>Quantity sold</th>
            <th>Revenue</th>
        </tr>
        </thead>
        <tbody>
        <#list orders as order>
            <tr>
                <td>${order.dateCreated}</td>
                <td>${order.quantity}</td>
                <td>${order.status}</td>
                <td>${currency} ${order.revenue}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</div>

<div class="footer">
    <div class="container">
        &copy; 2025 Agriconnect | All rights reserved.
    </div>
</div>

</body>
</html>
