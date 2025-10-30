# 🌿 EUDR Role-Based Navigation Design

## Current Problem

Both farmers and exporters currently see the same EUDR tabs, which doesn't align with their different responsibilities in the compliance chain.

## 🎯 **Corrected Role-Based Design**

### 👨‍🌾 **FARMER PORTAL - EUDR Section**

**Role**: Data Provider & Primary Compliance Actor
**Responsibility**: Provide accurate data about their own farming operations

```javascript
// FARMER EUDR NAVIGATION
{
  icon: 'mdi-shield-check',
  text: 'My EUDR Status',
  link: { name: 'FarmerEudrStatus' },
  roles: ['FARMER'],
  iconColor: '#dc2626',
},
{
  icon: 'mdi-map-marker-radius',
  text: 'My Production Units',
  link: { name: 'FarmerProductionUnits' },
  roles: ['FARMER'],
  iconColor: '#16a34a',
},
{
  icon: 'mdi-file-document-multiple',
  text: 'My Documents',
  link: { name: 'FarmerDocuments' },
  roles: ['FARMER'],
  iconColor: '#0ea5e9',
},
{
  icon: 'mdi-satellite-variant',
  text: 'My Farm Monitoring',
  link: { name: 'FarmerMonitoring' },
  roles: ['FARMER'],
  iconColor: '#ea580c',
},
{
  icon: 'mdi-package-variant',
  text: 'My Batches',
  link: { name: 'FarmerBatches' },
  roles: ['FARMER'],
  iconColor: '#7c3aed',
},
```

### 🏭 **EXPORTER PORTAL - EUDR Section**

**Role**: Compliance Overseer & Risk Manager
**Responsibility**: Ensure all suppliers meet EUDR requirements

```javascript
// EXPORTER EUDR NAVIGATION
{
  icon: 'mdi-shield-account',
  text: 'EUDR Compliance Dashboard',
  link: { name: 'ExporterEudrDashboard' },
  roles: ['EXPORTER'],
  iconColor: '#dc2626',
},
{
  icon: 'mdi-account-group',
  text: 'Supplier Compliance',
  link: { name: 'SupplierCompliance' },
  roles: ['EXPORTER'],
  iconColor: '#16a34a',
},
{
  icon: 'mdi-alert-octagon',
  text: 'Risk Management',
  link: { name: 'RiskManagement' },
  roles: ['EXPORTER'],
  iconColor: '#ea580c',
},
{
  icon: 'mdi-file-chart',
  text: 'Compliance Reports',
  link: { name: 'ComplianceReports' },
  roles: ['EXPORTER'],
  iconColor: '#0ea5e9',
},
{
  icon: 'mdi-map-search',
  text: 'Supply Chain Mapping',
  link: { name: 'SupplyChainMapping' },
  roles: ['EXPORTER'],
  iconColor: '#7c3aed',
},
{
  icon: 'mdi-gavel',
  text: 'Due Diligence',
  link: { name: 'DueDiligence' },
  roles: ['EXPORTER'],
  iconColor: '#f59e0b',
},
```

## 📊 **Detailed Functionality by Role**

### 👨‍🌾 **FARMER CAPABILITIES**

#### **My EUDR Status**

- ✅ View personal compliance score
- ✅ See required actions for compliance
- ✅ Track document submission status
- ❌ Cannot see other farmers' data
- ❌ Cannot approve/reject anything

#### **My Production Units**

- ✅ Register new production units
- ✅ Update GPS coordinates
- ✅ Upload land ownership certificates
- ✅ View deforestation alerts for their land
- ❌ Cannot see other farmers' units
- ❌ Cannot modify verification status

#### **My Documents**

- ✅ Upload compliance documents
- ✅ View document verification status
- ✅ Download their own documents
- ✅ Receive feedback on document issues
- ❌ Cannot see other farmers' documents
- ❌ Cannot verify documents

#### **My Farm Monitoring**

- ✅ View satellite imagery of their farms
- ✅ See deforestation alerts
- ✅ Submit explanations for alerts
- ✅ View historical monitoring data
- ❌ Cannot see other farms
- ❌ Cannot dismiss alerts

#### **My Batches**

- ✅ Create new commodity batches
- ✅ Link batches to production units
- ✅ Transfer batches to buyers/processors
- ✅ View batch history
- ❌ Cannot see other farmers' batches
- ❌ Cannot modify transferred batches

### 🏭 **EXPORTER CAPABILITIES**

#### **EUDR Compliance Dashboard**

- ✅ Overview of all suppliers' compliance status
- ✅ Risk heat map by region/supplier
- ✅ Compliance deadlines and alerts
- ✅ Regulatory update notifications
- ✅ Export readiness indicators

#### **Supplier Compliance**

- ✅ View all farmers' compliance status
- ✅ Approve/reject farmer registrations
- ✅ Set compliance requirements
- ✅ Monitor supplier performance
- ✅ Manage supplier relationships

#### **Risk Management**

- ✅ View all deforestation alerts
- ✅ Assess risk levels by region
- ✅ Create mitigation plans
- ✅ Track risk reduction progress
- ✅ Generate risk reports

#### **Compliance Reports**

- ✅ Generate EUDR compliance reports
- ✅ Export data for regulatory submission
- ✅ Create audit trails
- ✅ Schedule automated reports
- ✅ Share reports with authorities

#### **Supply Chain Mapping**

- ✅ Visualize entire supply chain
- ✅ Track commodity flows
- ✅ Identify supply chain gaps
- ✅ Map high-risk areas
- ✅ Plan supply chain improvements

#### **Due Diligence**

- ✅ Conduct supplier due diligence
- ✅ Verify supplier documentation
- ✅ Assess supplier risk profiles
- ✅ Create due diligence reports
- ✅ Track corrective actions

## 🔄 **Information Flow**

### **Farmer → Exporter Flow**

1. **Farmer** uploads documents and data
2. **System** validates and processes
3. **Exporter** reviews and approves/rejects
4. **Farmer** receives feedback and takes action
5. **Exporter** monitors ongoing compliance

### **Exporter → Regulator Flow**

1. **Exporter** aggregates all supplier data
2. **System** generates compliance reports
3. **Exporter** submits to regulators
4. **Regulators** audit and verify
5. **Exporter** implements corrective actions

## 🎨 **Updated Navigation Structure**

```javascript
// FARMER EUDR SECTION
{
  icon: 'mdi-shield-check',
  text: 'My EUDR Status',
  link: { name: 'FarmerEudrStatus' },
  roles: ['FARMER'],
  iconColor: '#dc2626',
},
{
  icon: 'mdi-map-marker-radius',
  text: 'My Production Units',
  link: { name: 'FarmerProductionUnits' },
  roles: ['FARMER'],
  iconColor: '#16a34a',
},
{
  icon: 'mdi-file-document-multiple',
  text: 'My Documents',
  link: { name: 'FarmerDocuments' },
  roles: ['FARMER'],
  iconColor: '#0ea5e9',
},
{
  icon: 'mdi-satellite-variant',
  text: 'My Farm Monitoring',
  link: { name: 'FarmerMonitoring' },
  roles: ['FARMER'],
  iconColor: '#ea580c',
},
{
  icon: 'mdi-package-variant',
  text: 'My Batches',
  link: { name: 'FarmerBatches' },
  roles: ['FARMER'],
  iconColor: '#7c3aed',
},

// EXPORTER EUDR SECTION
{
  icon: 'mdi-shield-account',
  text: 'EUDR Dashboard',
  link: { name: 'ExporterEudrDashboard' },
  roles: ['EXPORTER'],
  iconColor: '#dc2626',
},
{
  icon: 'mdi-account-group',
  text: 'Supplier Compliance',
  link: { name: 'SupplierCompliance' },
  roles: ['EXPORTER'],
  iconColor: '#16a34a',
},
{
  icon: 'mdi-alert-octagon',
  text: 'Risk Management',
  link: { name: 'RiskManagement' },
  roles: ['EXPORTER'],
  iconColor: '#ea580c',
},
{
  icon: 'mdi-file-chart',
  text: 'Compliance Reports',
  link: { name: 'ComplianceReports' },
  roles: ['EXPORTER'],
  iconColor: '#0ea5e9',
},
{
  icon: 'mdi-map-search',
  text: 'Supply Chain Mapping',
  link: { name: 'SupplyChainMapping' },
  roles: ['EXPORTER'],
  iconColor: '#7c3aed',
},
{
  icon: 'mdi-gavel',
  text: 'Due Diligence',
  link: { name: 'DueDiligence' },
  roles: ['EXPORTER'],
  iconColor: '#f59e0b',
},
```

## 🎯 **Key Benefits of This Design**

### **For Farmers**

- ✅ Clear focus on their own operations
- ✅ Simple, actionable interface
- ✅ Direct feedback on compliance status
- ✅ No overwhelming information from other farms

### **For Exporters**

- ✅ Comprehensive oversight capabilities
- ✅ Risk management tools
- ✅ Regulatory reporting features
- ✅ Supply chain visibility

### **For Compliance**

- ✅ Clear separation of responsibilities
- ✅ Proper data access controls
- ✅ Audit trail maintenance
- ✅ Regulatory alignment

## 🚀 **Implementation Priority**

1. **Phase 1**: Update navigation structure
2. **Phase 2**: Implement role-based views
3. **Phase 3**: Add exporter oversight features
4. **Phase 4**: Integrate regulatory reporting

This design ensures that each role has the appropriate tools and information for their specific responsibilities in EUDR compliance, while maintaining the overall goal of helping exporters comply with EU regulations.
