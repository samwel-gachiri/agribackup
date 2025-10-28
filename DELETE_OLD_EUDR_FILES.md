# Delete Old EUDR Files

Run these PowerShell commands to remove the old, non-functional EUDR files:

```powershell
# Delete unused views
Remove-Item "src/views/eudr/BatchManagement.vue" -ErrorAction SilentlyContinue
Remove-Item "src/views/eudr/ComplianceDashboard.vue" -ErrorAction SilentlyContinue
Remove-Item "src/views/eudr/DeforestationMonitoring.vue" -ErrorAction SilentlyContinue
Remove-Item "src/views/eudr/DocumentManagement.vue" -ErrorAction SilentlyContinue
Remove-Item "src/views/eudr/ProductionUnits.vue" -ErrorAction SilentlyContinue

# Delete unused components
Remove-Item "src/components/eudr/ProductionUnitViewer.vue" -ErrorAction SilentlyContinue
Remove-Item "src/components/eudr/ProductionUnitManager.vue" -ErrorAction SilentlyContinue
Remove-Item "src/components/eudr/ProductionUnitForm.vue" -ErrorAction SilentlyContinue
Remove-Item "src/components/eudr/ProductionUnitCard.vue" -ErrorAction SilentlyContinue
Remove-Item "src/components/eudr/DeforestationMonitoringDashboard.vue" -ErrorAction SilentlyContinue

Write-Host "✓ Deleted 10 old EUDR files successfully!" -ForegroundColor Green
```

Or run all at once:

```powershell
@(
    "src/views/eudr/BatchManagement.vue",
    "src/views/eudr/ComplianceDashboard.vue",
    "src/views/eudr/DeforestationMonitoring.vue",
    "src/views/eudr/DocumentManagement.vue",
    "src/views/eudr/ProductionUnits.vue",
    "src/components/eudr/ProductionUnitViewer.vue",
    "src/components/eudr/ProductionUnitManager.vue",
    "src/components/eudr/ProductionUnitForm.vue",
    "src/components/eudr/ProductionUnitCard.vue",
    "src/components/eudr/DeforestationMonitoringDashboard.vue"
) | ForEach-Object {
    if (Test-Path $_) {
        Remove-Item $_ -Force
        Write-Host "✓ Deleted $_" -ForegroundColor Green
    } else {
        Write-Host "⚠ File not found: $_" -ForegroundColor Yellow
    }
}
```

**Important:** Run these commands from the `farmer-portal-frontend` directory.
