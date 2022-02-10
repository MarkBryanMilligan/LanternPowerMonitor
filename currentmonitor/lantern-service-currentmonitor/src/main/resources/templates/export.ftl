<div class="row">
    <div class="col-1 col-lg-3 col-4k-4"></div>
    <div class="col-10 col-lg-6 col-4k-4">
        <div class="row mt-3">
            <table class="table">
                <tbody>
                <#list months as month>
                    <#if month.progress == 0>
                        <form method="POST">
                    </#if>
                    <tr>
                        <td>${month.name!}</td>
                        <#if month.progress == 0>
                            <td><input type="hidden" name="month" value="${month.date}"/><input type="submit" class="btn-primary border-none px-2 py-1" value="Export"/></td>
                        <#elseif month.progress == 1>
                            <td>Queued</td>
                        <#elseif month.progress < 100>
                            <td>Progress ${month.progress!}%</td>
                        <#else>
                            <td>
                                <a href="export/${month.date}/${month.fileName!}.bson.zip" class="btn-primary border-none text-decoration-none p-2" download>BSON</a>
                                <a href="export/${month.date}/${month.fileName!}.json.zip" class="btn-primary border-none text-decoration-none p-2" download>JSON</a>
                                <a href="export/${month.date}/${month.fileName!}.csv.zip" class="btn-primary border-none text-decoration-none p-2" download>CSV</a>
                            </td>
                        </#if>
                    </tr>
                    <#if month.progress == 0>
                        </form>
                    </#if>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
    <div class="col-1 col-lg-3 col-4k-4"></div>
</div>
